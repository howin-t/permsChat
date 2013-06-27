package com.howintsui.mc.permsChat;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;

public final class PermsChat extends JavaPlugin {
    public static Chat chat = null;
    public static Permission permission = null;
    public YamlConfiguration config;
    private PermsChatListener listener;

    public void onEnable() {
        //setup the config
        setupConfig();

        //Chatlistener - can you hear me?
        this.listener = new PermsChatListener(this);
        this.getServer().getPluginManager().registerEvents(listener, this);

        //Vault chat hooks
        setupChat();
        
        //Vault permission hooks (for primary group search)
        setupPermission();


        System.out.println("[permsChat] Enabled!");
    }
    public void reload(){
        setupConfig();
    }
    
    private void setupConfig() {
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        try {
            if (!configFile.exists()) {
                this.saveDefaultConfig();
            }
        } catch (Exception ex) {
            Logger.getLogger(PermsChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        config = new YamlConfiguration();
        config = YamlConfiguration.loadConfiguration(configFile);

    }

    /*
     * Code to setup the Chat variable in Vault. Allows me to hook to all the prefix plugins.
     */
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupPermission(){
    	RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        return (permission != null);
    }

    //
    //  Begin methods from Functions.java
    //
    public String replacePlayerPlaceholders(Player player, String format) {
        String worldName = player.getWorld().getName();
        if(config.getBoolean("toggles.factions-support")){
            format = format.replace("%faction", this.getPlayerFaction(player));
        }
        return format.replace("%prefix", this.getPlayerPrefix(player))
                .replace("%suffix", this.getPlayerSuffix(player))
                .replace("%world", worldName)
                .replace("%displayname", player.getDisplayName())
                .replace("%player", player.getName());
    }
    
    private String getPlayerPrefix(Player player){
        List<String> perms = config.getStringList("fixes.preperms");
        String prefix = "";
        for (String p : perms){
        	if (player.hasPermission(p)){
        		String[] splitted = p.split("\\.");
        		prefix = config.getString("fixes.prefixes."+splitted[2]);
        		break;
        	}
        }
        return prefix;
    	/*String prefix = chat.getPlayerPrefix(player);
    	if(prefix == null || prefix.equals("")){
    		String group = permission.getPrimaryGroup(player);
    		prefix = chat.getGroupPrefix(player.getWorld().getName(),group);
    		if(prefix == null){
    			prefix = "";
    		}
    	}
    	return prefix;*/
    }
    
    private String getPlayerSuffix(Player player){
    	/*String suffix = chat.getPlayerSuffix(player);
    	if(suffix == null || suffix.equals("")){
    		String group = permission.getPrimaryGroup(player);
    		suffix = chat.getGroupPrefix(player.getWorld().getName(),group);
    		if(suffix == null){
    			suffix = "";
    		}
    	}
    	return suffix;*/
        List<String> perms = config.getStringList("fixes.sufperms");
        String suffix = "";
        for (String p : perms){
        	if (player.hasPermission(p)){
        		String[] splitted = p.split("\\.");
        		suffix = config.getString("fixes.suffixes."+splitted[2]);
        		break;
        	}
        }
        return suffix;
    }

    private String getPlayerFaction(Player player){
        String tag = "";
        try{
            FPlayer fp = FPlayers.i.get(player); //import com.massivecraft.factions.FPlayer/FPlayers to get a FPlayer (base object for most factions functions) from a bukkit player
            Faction faction =  fp.getFaction(); //to get the faction of a fplayer
            tag = faction.getTag();
        }catch (Exception e){
            System.out.println("Factions plugin not found");
        }
        return tag;
    }

    public String colorize(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("&([a-z0-9])", "\u00A7$1");
    }

    public List<Player> getLocalRecipients(Player sender, double range) {
        Location playerLocation = sender.getLocation();
        List<Player> recipients = new LinkedList<Player>();
        double squaredDistance = Math.pow(range, 2);
        for (Player recipient : getServer().getOnlinePlayers()) {
            // Recipient are not from same world
            if (!recipient.getWorld().equals(sender.getWorld())) {
                continue;
            }
            if (playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance) {
                continue;
            }
            recipients.add(recipient);
        }
        return recipients;
    }

    public List<Player> getSpies() {
        List<Player> recipients = new LinkedList<Player>();
        for (Player recipient : this.getServer().getOnlinePlayers()) {
            if (recipient.hasPermission("permsChat.spy")) {
                recipients.add(recipient);
            }
        }
        return recipients;
    }
}
