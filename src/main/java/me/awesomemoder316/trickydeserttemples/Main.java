package me.awesomemoder316.trickydeserttemples;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main extends JavaPlugin implements Listener {
    private boolean update = false;
    private boolean firstUpdateCheck = true;
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        new Metrics(this, 11268);

        try {
            updateChecker();
        } catch (IOException e) {
            e.printStackTrace();
        }

        repeatUpdateChecker();
    }

    @EventHandler
    public void newDesertPyramid(ChunkLoadEvent e) {
        if (!e.isNewChunk()) return;
        Chunk chunk = e.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 130; y++) { //Checking only for desert pyramid, no excessive air check needed.
                for (int z = 0; z < 16; z++) {
                    Block blocks = chunk.getBlock(x, y, z);
                    if (blocks.getType() == Material.TNT) {
                        int xCord = blocks.getX();
                        int yCord = blocks.getY();
                        int zCord = blocks.getZ();
                        if (e.getChunk().getWorld().getBlockAt(xCord + 1, yCord, zCord).getType() == Material.TNT && e.getChunk().getWorld().getBlockAt(xCord, yCord, zCord + 1).getType() == Material.TNT) {
                            int random = new Random().nextInt(2); //0, 1

                            if (random == 0) {
                                for (int a = 0; a < 3; ++a) {
                                    for (int b = 0; b < 3; ++b) {
                                        if (a != 1 || b != 1) {
                                            chunk.getBlock(x + a, y + 2, z + b).setType(Material.STONE_PRESSURE_PLATE);
                                        }
                                    }
                                }
                            }

                            random = new Random().nextInt(2);

                            if (random == 0) { //Make non-mutually exclusive
                                setChest(chunk, x - 1, y, z + 1);
                                setChest(chunk, x + 1, y, z - 1);
                                setChest(chunk, x + 3, y, z + 1);
                                setChest(chunk, x + 1, y, z + 3);
                            }
                            return;
                        }

                    }
                }
            }
        }
    }

    private void setChest(Chunk chunk, int x, int y, int z) {
        Block block = chunk.getBlock(x, y + 2, z);
        BlockData blockData = block.getBlockData();

        BlockFace blockFace = BlockFace.WEST;
        if (blockData.toString().contains("north")) {
            blockFace = BlockFace.NORTH;
        } else if (blockData.toString().contains("south")) {
            blockFace = BlockFace.SOUTH;
        } else if (blockData.toString().contains("east")) {
            blockFace = BlockFace.EAST;
        }

        Container container = (Container) block.getState();
        ItemStack[] toAdd = container.getInventory().getStorageContents();


        block.setType(Material.TRAPPED_CHEST);
        blockData = block.getBlockData(); //Redefine with new info
        ((Directional) blockData).setFacing(blockFace);

        container = (Container) block.getState();
        container.getInventory().setContents(toAdd);

        block.setBlockData(blockData);

        chunk.getBlock(x, y, z).setType(Material.TNT);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (update && e.getPlayer().hasPermission("trickyDesertTemples.updater")) {
            e.getPlayer().sendMessage(ChatColor.GOLD + "[Tricky Desert Temples] An update is available at https://www.curseforge.com/minecraft/bukkit-plugins/tricky-desert-temples!");
        }
    }

    public void updateChecker() throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/awesomemoder316/MinecraftPlugins/main/TrickyDesertTemples/spigot/version.txt");
        Scanner sc = new Scanner(url.openStream());
        StringBuffer sb = new StringBuffer();
        while (sc.hasNext()) {
            sb.append(sc.next());
        }
        String result = sb.toString();
        result = result.replaceAll("<[^>]*>", "");

        if (isInterger(result)) {

            String[] pluginVersionStringA = getDescription().getVersion().split("\\.");

            if (pluginVersionStringA[1].length() == 1) {
                pluginVersionStringA[1] = "0" + pluginVersionStringA[1];
            }
            if (pluginVersionStringA[2].length() == 1) {
                pluginVersionStringA[2] = "0" + pluginVersionStringA[2];
            }

            int currentVersion = Integer.parseInt(pluginVersionStringA[0] + pluginVersionStringA[1] + pluginVersionStringA[2]);


            int newVersion = Integer.parseInt(result);
            if ((newVersion - currentVersion) <= 0) {
                if (firstUpdateCheck) {
                    System.out.println("[Tricky Desert Temples] " + ChatColor.BLUE + "On latest version!");
                    firstUpdateCheck = false; //So that console not spammed every 2 hours.
                }
            } else {
                System.out.println("[Tricky Desert Temples] " + ChatColor.BLUE + "A new version is available at https://www.curseforge.com/minecraft/bukkit-plugins/tricky-desert-temples!");
                update = true;
            }
        }
    }

    public void repeatUpdateChecker() {
        if (!update) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        updateChecker();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!update) {
                        repeatUpdateChecker(); //Schedule new check.
                    }
                }
            }.runTaskLater(this, 3456000); //Checks every 48 hours.
        }
    }

    private boolean isInterger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}