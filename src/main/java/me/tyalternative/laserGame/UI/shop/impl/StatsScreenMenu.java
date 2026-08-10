package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.HologramElement;
import me.tyalternative.laserGame.UI.shop.HologramScrollType;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.utils.TextUtil;
import me.tyalternative.laserGame.utils.TickUtil;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;

public class StatsScreenMenu {


    private static final int LEADERBOARD_CAPACITY = 16;

    private HologramElement statsScreen;
    private final List<HologramElement> readyButtons = new ArrayList<>();
    private final List<HologramElement> leaderboardEntries = new ArrayList<>();
    private final List<HologramElement> leaderboardEntryTexts = new ArrayList<>();
    private final List<Double> yTranslations = new ArrayList<>();
    private HologramElement leaderboardScroller;
    private double initialInteractionYCoord;
    private long lastScroll = -1;

    private HologramElement shotFiredStatDisplay;
    private HologramElement killStatDisplay;
    private HologramElement eliminationStatDisplay;
    private HologramElement reloadStatDisplay;
    private HologramElement objectUsedStatDisplay;
    private HologramElement moneyEarnedStatDisplay;

    int currentIndex = 0;
    private int visibleLeaderboardCount = 0;

    public StatsScreenMenu(HologramElement parent) {
        this.statsScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return statsScreen; }

    public List<HologramElement> getReadyButtons() { return readyButtons; }

    public void refresh(GamePlayer gamePlayer, List<GamePlayer> matchPlayers) {

        shotFiredStatDisplay.setText(formatCount(gamePlayer.getShotsFired()));
        killStatDisplay.setText(formatCount(gamePlayer.getKills()));
        eliminationStatDisplay.setText(formatCount(gamePlayer.getEliminations()));
        reloadStatDisplay.setText(formatCount(gamePlayer.getReloads()));
        objectUsedStatDisplay.setText(formatCount(gamePlayer.getConsumablesUsed()));
        moneyEarnedStatDisplay.setText(formatCount(gamePlayer.getTotalMoneyEarned()));

        List<GamePlayer> ranked = new ArrayList<>(matchPlayers);
        ranked.sort(Comparator.comparingInt(GamePlayer::getRoundWins).reversed());

        visibleLeaderboardCount = Math.min(ranked.size(), leaderboardEntries.size());

        for (int i = 0; i < leaderboardEntries.size(); i++) {
            HologramElement entry = leaderboardEntries.get(i);
            HologramElement entryText = leaderboardEntryTexts.get(i);

            if (i >= ranked.size()) {
                entry.setVisible(false);
                continue;
            }
            entry.setVisible(true);

            GamePlayer ranked_gp = ranked.get(i);
            Player p = ranked_gp.getPlayer();
            String name = p != null ? p.getName() : "???";
            entryText.setText(buildLeaderboardLine(name, ranked_gp.getRoundWins()));
        }

        currentIndex = 0;
        updateLeaderboardPosition();
    }

    private String formatCount(int value) {
        if (value >= 10000) return String.format("%02fk", value / 1000.0);
        if (value >= 1000) return String.format("%.1fk", value / 1000.0);
        return String.valueOf(value);
    }

    private String buildLeaderboardLine(String name, int roundWins) {
        int roundWinLength = TextUtil.getStringLength(String.valueOf(roundWins));
        int nameLength = TextUtil.getStringLength(name);
        int margin = 83 - nameLength - roundWinLength;
        String displayName = name;
        while (margin <= 3 && !displayName.isEmpty()) {
            displayName = displayName.substring(0, displayName.length() - 1);
            nameLength = TextUtil.getStringLength(displayName);
            margin = 83 - nameLength - roundWinLength;
        }
        return displayName + (margin % 2 == 0 ? "¤" : " ") + ".".repeat(Math.max(0, Math.floorDiv(margin - 1, 2))) + " " + roundWins;
    }

    private HologramElement createHologram(HologramElement parent) {

        HologramElement statsScreen = new HologramElement.Builder("stats_screen", parent)
                .position(139, 29).size(28, 18).layer(5)
                .font(Font.STATS)
                .text("\uE2FF").hoverText("\uE3FF")
                .button()
                .build();

        HologramElement shopScreenButtonLink = new HologramElement.Builder("shop_screen_button_link_from_stats", statsScreen)
                .position(110, 29).size(28, 18).layer(0)
                .font(Font.SHOP)
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();
        HologramElement inventoryScreenButtonLink = new HologramElement.Builder("inventory_screen_button_link_from_stats", statsScreen)
                .position(81, 29).size(28, 18).layer(0)
                .font(Font.INVENTORY)
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();

        HologramElement background = new HologramElement.Builder("status_screen_background_texture", statsScreen)
                .position(0,137).size(212,138).layer(-1)
                .text("\uFFFF")
                .build();


        HologramElement readyButton = new HologramElement.Builder("ready_button_stats", background)
                .position(32,125).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100").font(Font.MENU).dontPropagateFont(true)
                .button()
                .build();

        HologramElement leaderboardContainer = new HologramElement.Builder("leaderboardContainer", background)
                .position(95,123).size(104,87).layer(-3)
                .button()
                .build();

        readyButtons.add(readyButton);


        shotFiredStatDisplay = new HologramElement.Builder("shot_fired_stat_display", background)
                .position(20,65).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();
        killStatDisplay = new HologramElement.Builder("kill_stat_display", background)
                .position(46,65).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();
        eliminationStatDisplay = new HologramElement.Builder("elimination_stat_display", background)
                .position(72,65).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();
        reloadStatDisplay = new HologramElement.Builder("reload_stat_display", background)
                .position(20,99).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();
        objectUsedStatDisplay = new HologramElement.Builder("object_used_stat_display", background)
                .position(46,99).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();
        moneyEarnedStatDisplay = new HologramElement.Builder("money_earned_stat_display", background)
                .position(72,99).size(13,5).layer(1)
                .text("000")
                .font(Font.TEXT)
                .build();


        for (int indexPlacement = 0; indexPlacement < LEADERBOARD_CAPACITY; indexPlacement++) {

            HologramElement leaderboardEntry = new HologramElement.Builder("leaderboard_entry_" + indexPlacement, leaderboardContainer)
                    .position(98,134).size(89,11).layer(1)
                    .text("\uE000").hoverText("\uE100")
                    .bubbleScrollToParent(true)
                    .button()
                    .build();

            leaderboardEntries.add(leaderboardEntry);
            initialInteractionYCoord = leaderboardEntry.getInteraction().getY();

            HologramElement leaderboardEntryText = new HologramElement.Builder("leaderboard_entry_text_" + indexPlacement, leaderboardEntry)
                    .position(101,132).size(83,5).layer(1)
                    .text("").font(Font.TEXT)
                    .bubbleScrollToParent(true)
                    .build();
            leaderboardEntryTexts.add(leaderboardEntryText);
        }


        leaderboardScroller = new HologramElement.Builder("leaderboard_scroller", leaderboardContainer)
                .position(193,55).size(4,17).layer(4)
                .text("\uE001")
                .bubbleScrollToParent(true)
                .build();


        leaderboardContainer.onScroll((player, source, scrollType) -> {

            if (scrollType == HologramScrollType.DOWN && currentIndex < Math.max(0, visibleLeaderboardCount-6)) {
                currentIndex = Math.min(Math.max(0, visibleLeaderboardCount-1), currentIndex+1);
                updateLeaderboardPosition();
            }
            else if (scrollType == HologramScrollType.UP && currentIndex > 0) {
                currentIndex = Math.max(0, currentIndex-1);
                updateLeaderboardPosition();
            }
        });

        TickUtil.delay(this::updateLeaderboardPosition,5);

        return statsScreen;
    }

    private void updateLeaderboardPosition() {
        for (int i = 0; i < leaderboardEntries.size(); i++) {
            HologramElement leaderboardEntry = leaderboardEntries.get(i);
            double posY = Math.min(7,Math.max(0, 6 - i + currentIndex)) * 14 * 0.025;
            try {
                if ( yTranslations.get(i) == posY) continue;
            } catch (Exception e) {
                yTranslations.add(posY);
            }

            leaderboardEntry.setYTranslation(posY, 3);
//            leaderboardEntry.getInteraction().teleport(leaderboardEntry.resolveInteractionLocation());
            leaderboardEntry.getChildren().getFirst().setYTranslation(posY, 3);
            yTranslations.set(i, posY);
        }

        double scrollerPosY = currentIndex * 1.65 / Math.max(1, visibleLeaderboardCount-6);
        leaderboardScroller.setYTranslation(-scrollerPosY, 3);
    }

}