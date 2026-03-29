package com.cortezromeo.taixiu.support;

import com.cortezromeo.taixiu.TaiXiu;
import com.cortezromeo.taixiu.api.storage.ISession;
import com.cortezromeo.taixiu.manager.DatabaseManager;
import com.cortezromeo.taixiu.manager.TaiXiuManager;
import com.cortezromeo.taixiu.util.MessageUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.events.ExpansionUnregisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlaceholderAPISupport extends PlaceholderExpansion implements Listener {

    @EventHandler
    public void onExpansionUnregister(ExpansionUnregisterEvent event) {
        if (event.getExpansion().getIdentifier().equalsIgnoreCase(getIdentifier())) {
            // Delay 1 tick để PlaceholderAPI hoàn tất quá trình reload trước khi đăng ký lại
            Bukkit.getScheduler().runTaskLater(TaiXiu.plugin, () -> {
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    new PlaceholderAPISupport().register();
                    MessageUtil.log("&a[TAI XIU] PlaceholderAPI expansion re-registered after reload.");
                }
            }, 5L);
        }
    }

    @Override
    public String getAuthor() {
        return "Cortez_Romeo";
    }

    @Override
    public String getIdentifier() {
        return "taixiu";
    }

    @Override
    public String getVersion() {
        return TaiXiu.plugin.getDescription().getVersion();
    }


    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public boolean canRegister() {
        return TaiXiu.plugin != null && TaiXiu.plugin.isEnabled();
    }

    @Override
    public String onPlaceholderRequest(org.bukkit.entity.Player player, String s) {
        if (s == null)
            return null;

        if (s.equals("phien") || s.equals("currentsession"))
            return String.valueOf(TaiXiuManager.getSessionData().getSession());


        if (s.equals("timeleft")) {
            return String.valueOf(TaiXiuManager.getTimeLeft());
        }

        if (s.startsWith("result_phien_")) {
            String sessionNumber = s.replace("result_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";
            return String.valueOf(session.getResult());
        }

        if (s.startsWith("resultformat_phien_")) {
            String sessionNumber = s.replace("resultformat_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";
            return com.cortezromeo.taixiu.util.MessageUtil.getFormatResultName(session.getResult());
        }

        if (s.startsWith("taiplayers_phien_")) {
            String sessionNumber = s.replace("taiplayers_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";
            return String.valueOf(session.getTaiPlayers());
        }

        if (s.startsWith("xiuplayers_phien_")) {
            String sessionNumber = s.replace("xiuplayers_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";
            return String.valueOf(session.getXiuPlayers());
        }

        if (s.startsWith("taiplayers_bet_phien_")) {
            String sessionNumber = s.replace("taiplayers_bet_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";

            long sum = 0L;
            if (session.getTaiPlayers() != null) {
                for (long value : session.getTaiPlayers().values()) {
                    sum += value;
                }
            }
            return String.valueOf(sum);
        }

        if (s.startsWith("xiuplayers_bet_phien_")) {
            String sessionNumber = s.replace("xiuplayers_bet_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";

            long sum = 0L;
            if (session.getXiuPlayers() != null) {
                for (long value : session.getXiuPlayers().values()) {
                    sum += value;
                }
            }
            return String.valueOf(sum);
        }

        if (s.startsWith("totalbet_phien_")) {
            String sessionNumber = s.replace("totalbet_phien_", "");
            if (sessionNumber.equals("current") || sessionNumber.equals("hientai"))
                sessionNumber = String.valueOf(TaiXiuManager.getSessionData().getSession());

            ISession session = getSessionSafe(sessionNumber);
            if (session == null) return "";
            return String.valueOf(TaiXiuManager.getTotalBet(session));
        }

        return null;
    }


    private ISession getSessionSafe(String sessionNumberStr) {
        try {
            long sessionNumber = Long.parseLong(sessionNumberStr);

            if (sessionNumber == TaiXiuManager.getSessionData().getSession()) {
                return TaiXiuManager.getSessionData();
            }

            if (DatabaseManager.taiXiuData.containsKey(sessionNumber)) {
                return DatabaseManager.taiXiuData.get(sessionNumber);
            }

            if (DatabaseManager.checkExistsFileData(sessionNumber)) {
                DatabaseManager.loadSessionData(sessionNumber);
                return DatabaseManager.taiXiuData.get(sessionNumber);
            }

            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}