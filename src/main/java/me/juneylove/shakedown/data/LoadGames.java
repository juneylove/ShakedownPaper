package me.juneylove.shakedown.data;

public class LoadGames {
/*
    public static HashMap<String, GameSetting> LoadFromFolder() {

        Plugin plugin = Main.getInstance();

        File gameFolder = new File(plugin.getDataFolder(), "games");
        File[] gameFiles = gameFolder.listFiles();

        HashMap<String, GameSetting> gameOptions = new HashMap<>();

        if (gameFiles != null) {

            for (File gameFile : gameFiles) {

                // Initializes to default settings
                GameSetting gameSetting = new Lobby();

                // File loaded, load each game
                YamlConfiguration data = YamlConfiguration.loadConfiguration(gameFile);

                Set<String> keys = data.getKeys(false);

                if (keys.contains("name")) {
                    gameSetting.name = data.getString("name");
                }



                if (keys.contains("friendlyFireEnabled")) {
                    gameSetting.friendlyFireEnabled = data.getBoolean("friendlyFireEnabled");
                }
                if (keys.contains("pvpDingEnabled")) {
                    gameSetting.pvpDingEnabled = data.getBoolean("pvpDingEnabled");
                }
                if (keys.contains("deathFireworkEnabled")) {
                    gameSetting.deathFireworkEnabled = data.getBoolean("deathFireworkEnabled");
                }
                if (keys.contains("pvpKillScore")) {
                    gameSetting.pvpKillScore = data.getInt("pvpKillScore");
                }
                if (keys.contains("predeterminedDamageHp")) {
                    gameSetting.predeterminedDamageHp = data.getInt("predeterminedDamageHp");
                }
                if (keys.contains("pvpDamageScore")) {
                    gameSetting.pvpDamageScore = data.getInt("pvpDamageScore");
                }

                if (keys.contains("keepInventoryOnDeath")) {
                    gameSetting.keepInventoryOnDeath = data.getBoolean("keepInventoryOnDeath");
                }
                if (keys.contains("keepInventoryOnFinalDeath")) {
                    gameSetting.keepInventoryOnFinalDeath = data.getBoolean("keepInventoryOnFinalDeath");
                }
                if (keys.contains("respawnTimeSeconds")) {
                    gameSetting.respawnTimeSeconds = data.getInt("respawnTimeSeconds");
                }
                if (keys.contains("numberOfLives")) {
                    gameSetting.currentRound.numberOfLives = data.getInt("numberOfLives");
                }
                if (keys.contains("maxHealthHearts")) {
                    gameSetting.maxHealthHearts = data.getInt("maxHealthHearts");
                }
                if (keys.contains("hungerEnabled")) {
                    gameSetting.hungerEnabled = data.getBoolean("hungerEnabled");
                }

                if (keys.contains("blockDamageEnabled")) {
                    gameSetting.blockDamageEnabled = data.getBoolean("blockDamageEnabled");
                }
                if (keys.contains("fallDamageEnabled")) {
                    gameSetting.fallDamageEnabled = data.getBoolean("fallDamageEnabled");
                }
                if (keys.contains("dropItemsEnabled")) {
                    gameSetting.itemDropsEnabled = data.getBoolean("dropItemsEnabled");
                }
                if (keys.contains("moveItemsEnabled")) {
                    gameSetting.moveItemsEnabled = data.getBoolean("moveItemsEnabled");
                }
                if (keys.contains("entityInteractsEnabled")) {
                    gameSetting.entityInteractsEnabled = data.getBoolean("entityInteractsEnabled");
                }
                if (keys.contains("chestInteractsEnabled")) {
                    gameSetting.chestInteractsEnabled = data.getBoolean("chestInteractsEnabled");
                }

                if (keys.contains("immediateTntPrime")) {
                    gameSetting.immediateTntPrime = data.getBoolean("immediateTntPrime");
                }
                if (keys.contains("tntFuseTicks")) {
                    gameSetting.tntFuseTicks = data.getInt("tntFuseTicks");
                }
                if (keys.contains("tntDestroysBlocks")) {
                    gameSetting.tntDestroysBlocks = data.getBoolean("tntDestroysBlocks");
                }

                if (keys.contains("tntDestroysBlocks")) {
                    gameSetting.tntDestroysBlocks = data.getBoolean("tntDestroysBlocks");
                }
                if (keys.contains("placementRegion")) {
                    //gameOption.placementRegion = getPlacementRegion(data);
                }
                if (keys.contains("allowTntPlace")) {
                    gameSetting.allowTntPlace = data.getBoolean("allowTntPlace");
                }
                if (keys.contains("allowConcretePlace")) {
                    gameSetting.allowConcretePlace = data.getBoolean("allowConcretePlace");
                }
                if (keys.contains("allowCobwebPlace")) {
                    gameSetting.allowCobwebPlace = data.getBoolean("allowCobwebPlace");
                }
                if (keys.contains("allowOtherBlockPlace")) {
                    gameSetting.allowOtherBlockPlace = data.getBoolean("allowOtherBlockPlace");
                }
                if (keys.contains("infiniteConcrete")) {
                    gameSetting.infiniteConcrete = data.getBoolean("infiniteConcrete");
                }

                if (keys.contains("scoreboardType")) {
                    gameSetting.scoreboardType = getScoreboardType(data);
                }
                if (keys.contains("constantScoreboardRefresh")) {
                    gameSetting.constantScoreboardRefresh = data.getBoolean("constantScoreboardRefresh");
                }
                if (keys.contains("distributeScoresEvenly")) {
                    gameSetting.distributeScoresEvenly = data.getBoolean("distributeScoresEvenly");
                }
                if (keys.contains("teammateGlowEnabled")) {
                    gameSetting.teammateGlowEnabled = data.getBoolean("teammateGlowEnabled");
                }

                gameOptions.put(gameSetting.name, gameSetting);

            }

        }

        return gameOptions;

    }

    private static Sidebar.ScoreboardType getScoreboardType(YamlConfiguration data) {

        String value = data.getString("scoreboardType");

        if (value != null) {

            switch (value) {
                case "TEAM":
                    return Sidebar.ScoreboardType.TEAM;
                case "INDIVIDUAL":
                    return Sidebar.ScoreboardType.INDIVIDUAL;
                case "HYBRID":
                    return Sidebar.ScoreboardType.HYBRID;
            }

        }
        return Sidebar.ScoreboardType.HYBRID;

    }

    private static List<Integer> getPlacementRegion(YamlConfiguration data) {

        List<Integer> list = new ArrayList<>(6);

        list.add(1, data.getInt("placementRegion.minx"));
        list.add(2, data.getInt("placementRegion.maxx"));
        list.add(3, data.getInt("placementRegion.miny"));
        list.add(4, data.getInt("placementRegion.maxy"));
        list.add(5, data.getInt("placementRegion.minz"));
        list.add(6, data.getInt("placementRegion.maxz"));

        return list;

    }

*/
}
