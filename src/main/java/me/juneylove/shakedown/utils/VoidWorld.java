package me.juneylove.shakedown.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public class VoidWorld {

    @Nullable
    public World create(String name) {

        if (!worldNameIsValid(name)) return null;
        if (worldFolderExists(name)) return Bukkit.getWorld(name);

        return WorldCreator.name(name).generator(new VoidGenerator()).createWorld();

    }

    private boolean worldNameIsValid(String worldName) {

        // Illegal characters
        //noinspection RegExpRedundantEscape
        if (worldName.matches("^.*[/\n\r\t\0\f`?*\\<>|\":.].*$")) {
            return false;
        }

        // Illegal filenames in windows
        for (int i=1; i<=9; i++) {
            if (worldName.equals("COM" + i) || worldName.equals("LPT" + i)) {
                return false;
            }
        }

        // Probably a good idea to not allow these either
        if (worldName.endsWith("_nether") || worldName.endsWith("_the_end")) {
            return false;
        }

        // Check specific values
        return switch (worldName) {

            // Names of other non-world folders
            // Illegal filenames in windows
            case ".cache", "bundler", "logs", "plugins", "CON", "PRN", "AUX", "NUL" -> false;
            default -> true;

        };

    }

    public boolean worldFolderExists(String worldName) {

        File worldsFolder = Bukkit.getWorldContainer();
        if (worldsFolder.listFiles() != null) {

            for (File file : Objects.requireNonNull(worldsFolder.listFiles())) {
                if (file.getName().equals(worldName)) {
                    if (file.isDirectory()) return true;
                }
            }

        }
        return false;

    }

}
