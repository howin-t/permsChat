package com.howintsui.mc.permsChat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PermsChatListener implements Listener {

    public String MESSAGE_FORMAT = "%prefix %player: &f%message";
    public String LOCAL_MESSAGE_FORMAT = "[LOCAL] %prefix %player: &f%message";
    public Boolean RANGED_MODE = false;
    public double CHAT_RANGE = 100d;
    private final PermsChat plugin;

    public PermsChatListener(PermsChat aThis) {
        this.plugin = aThis;
        this.MESSAGE_FORMAT = plugin.config.getString("formats.message-format", this.MESSAGE_FORMAT);
        this.LOCAL_MESSAGE_FORMAT = plugin.config.getString("formats.local-message-format", this.LOCAL_MESSAGE_FORMAT);
        this.RANGED_MODE = plugin.config.getBoolean("toggles.ranged-mode", this.RANGED_MODE);
        this.CHAT_RANGE = plugin.config.getDouble("other.chat-range", this.CHAT_RANGE);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        String message = MESSAGE_FORMAT;
        String chatMessage = event.getMessage();

        boolean localChat = RANGED_MODE;
		if (chatMessage.startsWith("!@#vonBraun")){
			if (player.hasPermission("permsChat.vonBraun")){
			player.sendMessage("cocoChat reloading");
			plugin.reload();
			}
		} else if (localChat) {
        	if(!(chatMessage.startsWith("!"))){
                message = LOCAL_MESSAGE_FORMAT;
                double range = CHAT_RANGE;
                event.getRecipients().clear();
                event.getRecipients().addAll(plugin.getLocalRecipients(player, range));
                event.getRecipients().addAll(plugin.getSpies());
        	}else{
        		chatMessage = chatMessage.replaceFirst("!","");
        	}
        }

//        if (chatMessage.startsWith("@") && player.hasPermission("bchatmanager.chat.message")) {
//            chatMessage = chatMessage.substring(1);
//            String[] messageSplit = chatMessage.split(" ");
//            Player reciever = plugin.getServer().getPlayer(messageSplit[0]);
//            if (messageSplit[0] == "ops") {
//                chatMessage = chatMessage.replaceFirst(messageSplit[0], "");
//                chatMessage = chatMessage.replaceAll("%reciever", messageSplit[0]);
//
//                List<Player> recipients = new LinkedList<Player>();
//                event.getRecipients().clear();
//                event.getRecipients().add(player);
//
//                for (Player recipient : plugin.getServer().getOnlinePlayers()) {
//                    if (recipient.isOp()) {
//                        recipients.add(recipient);
//                    }
//                }
//
//                event.getRecipients().addAll(recipients);
//                message = PERSONAL_MESSAGE_FORMAT;
//            } else if (reciever == null) {
//                player.sendMessage("This player isn't online or you just typed the @ symbol! Ignoring.");
//                event.setCancelled(true);
//            } else {
//                chatMessage = chatMessage.replaceFirst(messageSplit[0], "");
//                message = PERSONAL_MESSAGE_FORMAT;
//                message = message.replaceAll("%reciever", messageSplit[0]);
//                localChat = false;
//                event.getRecipients().clear();
//                event.getRecipients().add(player);
//                event.getRecipients().add(reciever);
//                event.getRecipients().addAll(plugin.getSpies());
//                message = PERSONAL_MESSAGE_FORMAT;
//            }
//        }

        message = plugin.replacePlayerPlaceholders(player, message);
        message = plugin.colorize(message);

        if (player.hasPermission("permsChat.color")) {
            chatMessage = plugin.colorize(chatMessage);
        }

        message = message.replace("%message", chatMessage);

        event.setFormat(message);
        event.setMessage(chatMessage);

    }
}
