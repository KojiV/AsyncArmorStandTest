# Asynchronous Armor Stand Testing
Source Code for my testing of asynchronous Armor Stand movements.

I whipped this up to originally test for Koji's Skyblock without having to compile the entirety of the plugin every time. Because of this, the code isn't of the highest quality, and there are things I did just to save myself the time and the headache. So take this code with a litte grain of salt.

# Why?
By default, the easiest way to change the position of an entity is to teleport it. 

```
new BukkitRunnable() {
    @Override
    public void run() {
        // This will send the armorstand high into the sky.
        armorStand.teleport(location);
        location.add(0, 0.2, 0);
    }
}.runTaskTimer(plugin, 1L, 1L);
```

However, there are two issues with this:
1. If many teleports are done a second (or every tick), the teleport itself gets laggy and trips on itself.
2. You cannot execute the runnable or the teleport on a thread other than the main thread, possibly dropping TPS if the amount and intensity of the action is high.

To remedy this, some decide to instead set the velocity of the entity

```
new BukkitRunnable() {
    @Override
    public void run() {
        // This will send the armorstand high into the sky.
        armorStand.setVelocity(desiredLocation.clone().subtract(currentLocation));
        desiredLocation.add(0, 0.2, 0);
    }
}.runTaskTimer(plugin, 1L, 1L);
```

This is better, as you can now pawn it off to another thread instead of forcing it to handle itself, potentially boosting performance. This also fixes the issue of the visual lag. However, this method comes with two huge downsides:

1. **You can't rotate the entity**
2. You cannot move the entity move huge distances instantly

So from there you have two options:
1. Teleport the entity and do it on the main task
2. Use packets (I chose this)

# Packets
## Basic Overview
Instead of teleporting with the Bukkit API, NMS packets are instead used to teleport the armor stand. To avoid reflection, an interface (UncollidibleArmorStand) is used to house each version's NMS, then it selects which implemented version interface to use based on the version of the server. Normally, the armor stand would just be summoned via the Bukkit API and then edited through packets, but instead they are spawned via packets as well (more information as to why is in the next section).

The packets used are all version based variations of:
- PacketPlayOutSpawnEntityLiving
- PacketPlayOutEntityMetadata
- PacketPlayOutEntityEquipment
- PacketPlayOutEntityTeleport

Packets like EntityTeleport are required to move the armor stand, as what is happening is you are tricking your client into believing an armor stand exists, but the server doesn't recognize it, so more packets have to be used to continuously trick the client to moving and updating the armor stand.

## 1.17+
1.17 and any version above that is very pesky and annoying because of one reason: the package names are all identical. Because of this issue, compilers on compile time don't see anything wrong, but later, the servers run into issues due to the differences in method names. To remedy this issue, I use reflection in the 1.17+ armor stand class, which was very annoying. 

# Multiversion Stuff (ViaVersion)
This was basically the main source of my annoyance towards everything, and I will explain why here. 

## Overview
Starting in 1.9, the way armor stands hold skulls changed, so the angle of the armor stands change between 1.8 and 1.9. To make the pet always show upright correctly, packets are used to spawn only for certain players rather than others. Here is what happens if the arm pose is universal:
![Ew](https://github.com/KojiV/AsyncArmorStandTest/assets/69867605/6c326014-6ddb-424b-90f3-67c398a06cf1)
Obviously not super great. So to remedy this, individual data watchers are given to certain players depending on the version in order to always be upright to the player.

## Spawning and Visibility
Within the UncollidableArmorStand interface, a few core methods exist:
- setup
- spawn
- update
- move
- rotate
- destroy

All of these methods are essential if you want to properly setup everything, and so I'll explain what each does

### Setup
Setup is a method used when it is certain the entity hasn't been spawned, and basically creates the entity but doesn't spawn it in for anyone. It helps for setting the data of the armor stand without needing to send the spawn method AND the update method.

Setup is called with org.bukkit.World to initialize the entity into the selected world. No player will be able to see it.

Setup is used to allow for giving attributes and prevents accessing the Bukkit entity from returning null.
```
UncollidableArmorStand stand = AsyncArmorStandTest.getArmorStand();
stand.setup(world);

// If setup wasn't used this would throw an error because stand.getEntity() is null
ArmorStand entity = stand.getEntity();
entity.setGravity(false);
entity.setCustomNameVisible(true);
entity.setCustomName("Name Tag");

// Now instead of doing spawn into update, it can just be spawn
stand.spawn(world.getPlayers(), player.getLocation());
```

### Spawn
Spawn is a method used to display the entity for certain players. This is needed because the server itself doesn't recognize the entity, and will also allow for selective viewing of the entity.

Spawn can be called with many things, but the main method:
- **Returns:** LivingEntity
- **Arguments:**
  - **Collection<Player>** to represent the players the armor stand is being spawned for
  - **Location** to represent the location the armor stand will be spawned at.
  - **float[][]** to represent the rotations of the armor stand (but these in degrees not radians). Each takes 3 floats which represent the roll, yaw, and pitch of the armor stand. Each index represents:
    - 0: Head rotation
    - 1: Body rotation
    - 2: Left arm rotation
    - 3: Right arm rotation
    - 4: Left leg rotation
    - 5: Right leg rotation
  - **boolean** to represent whether to override the entity with a whole new one or not.

See example from setup to see how spawn is used.

### Update
Update is a method used to tell a group of players that the armor stand has changed elements of its metadata or equipment. Because the server doesn't actually know that the armor stand exists, the server will not change the metadata for you, so the sending of the update method (which sends a packet) is required.

Update can be called with multiple things, but the main method uses
- **Collection<Player>** to represent the players that will receive the armor stand update.
- **org.bukkit.inventory.ItemStack[]** to represent the equipment the armor stand will wear. The order goes like this:
  - Main hand
  - Offhand (this won't do anything for an armor stand)
  - Helmet
  - Chestplate
  - Leggings
  - Boots
- **Object** to represent the data watcher of the armor stand. Note that because it is an NMS class, the class type must be an Object due to the nature of interfaces.
- **boolean** to represent if the data watcher should even be applied to the entity.

```
ArmorStand itemStand = stand.getEntity();
if(visible) itemStand.setItemInHand(null);
else itemStand.setItemInHand(XMaterial.PLAYER_HEAD.parseItem());

// For the player (p), the item stand will now either be missing a head, or will have it returned to it.
stand.update(Collections.singleton(p), new float[][]{
        new float[3],
        new float[3],
        new float[3],
        { 100, 200, 140 },
        new float[3],
        new float[3]
}, false);
```

### Move
Move is a method that uses the teleport packet to move the armor stand. This is the only way for the stand to move, and travel across any distance. Because of its use of packets, the armor stand can be moved asynchronously in tasks.

Move's only limitation is moving across worlds, as for that an overriding spawn or setup method must be used to change it's world.

Move uses:
- **Collection<Player>** to represent the player who will see this change in position.
- **Location** to represent the x, y, z, yaw, and pitch. The original argument requires putting in all five, but there is a simpler version that uses Location.

```
// Repeating this over and over again moves it up 0.5 blocks for every player indefinitely.
Location newLoc = armorStand.getEntity().getLocation();
armorStand.move(newLoc.getWorld().getPlayers(), newLoc);

newLoc.add(0, 0.5, 0);
```

### Rotate
Rotate is a method that returns a data watcher representing the armor stand with the rotation values changed to match the inputted float.

The rotate method:
- **Returns:** Object (version specific DataWatcher)
- **Arguments:**
  - **float[][]** to represent the rotations of the armor stand (but these in degrees not radians). Each takes 3 floats which represent the roll, yaw, and pitch of the armor stand. Each index represents:
    - 0: Head rotation
    - 1: Body rotation
    - 2: Left arm rotation
    - 3: Right arm rotation
    - 4: Left leg rotation
    - 5: Right leg rotation

```
// this is used in a simplified version of the update method, and takes the floats inputted into a data watcher to set
ArmorStand stand = getEntity();
update(players, new ItemStack[] {
        stand.getItemInHand(),
        stand.getItemInHand(),
        stand.getHelmet(),
        stand.getChestplate(),
        stand.getLeggings(),
        stand.getBoots()
}, rotate(floats), setData);
```

### Destroy
Destroy is a method that makes the armor stand appear to not exist for some players. In the eyes of the client, the armor stand is wiped off of the game, and is not anywhere to be found.

Destroy uses only one argument:
- **Collection<Player>** to represent who the armor stand is destroyed for (anyone else who has the armor stand spawned will still be able to see it)

```
// Destroys the armor stand visually just for player
armorStand.destroy(Collections.singleton(player));
```