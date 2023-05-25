# Asynchronous Armor Stand Testing
Source Code for my testing of asynchronous Armor Stand movements.

I whipped this up to originally test for Koji's Skyblock without having to compile the entirety of the plugin every time. Because of this, the code isn't of the highest quality, and there are things I did just to have myself the time and the headache. So take this code with a litte grain of salt.

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
        armorStand.setVelocity(desiredLocation.subtract(currentLocation));
        desiredLocation.add(0, 0.2, 0);
    }
}.runTaskTimer(plugin, 1L, 1L);
```

This is better, as you can now pawn it off to another thread instead of forcing it to handle itself, potentially boosting performance. This also fixes the issue of the visual lag. However, this method comes with one huge downside:

**You can't rotate the entity**

So from there you have two options:
1. Teleport the entity and do it on the main task
2. Use packets (I chose this)

## Packets

