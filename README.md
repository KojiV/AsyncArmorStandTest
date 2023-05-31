# Asynchronous Armor Stand Testing
Source Code for my testing of asynchronous Armor Stand movements.

I whipped this up to originally test for Koji's Skyblock without having to compile the entirety of the plugin every time. Because of this, the code isn't of the highest quality, and there are things I did just to save myself the time and the headache. So take this code with a litte grain of salt.

## Why?
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

## Packets
### Basic Overview
Instead of teleporting with the Bukkit API, NMS packets are instead used to teleport the armor stand. To avoid reflection, an interface (UncollidibleArmorStand) is used to house each version's NMS, then it selects which implemented version interface to use based on the version of the server. Normally, the armor stand would just be summoned via the Bukkit API and then edited through packets, but instead they are spawned via packets as well (more information as to why is in the next section).

The packets used are all variations of:
- PacketPlayOutSpawnEntityLiving
- PacketPlayOutEntityMetadata
- PacketPlayOutEntityEquipment
- PacketPlayOutEntityTeleport

### 1.17+
1.17 and any version above that is very pesky and annoying because of one reason: the package names are all identical. Because of this issue, compilers on compile time don't see anything wrong, but later, the servers run into issues due to the differences in method names. To remedy this issue, I use reflection in the 1.17+ armor stand class, which was very annoying. 

## Multiversion Stuff (ViaVersion)
This was basically the main source of my annoyance towards everything, and I will explain why here. 

### Overview
Starting in 1.9, the way armor stands hold skulls changed, so the angle of the armor stands change between 1.8 and 1.9. To make the pet always show upright correctly, packets are used to spawn only for certain players rather than others. Here is what happens if the arm pose is universal:
