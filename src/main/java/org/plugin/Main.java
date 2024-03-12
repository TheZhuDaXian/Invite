package org.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin implements Listener, TabCompleter {
    private Map<String, User> users;  // 存储用户数据
    private Set<String> inviteCodes;  // 存储邀请码

    @Override
    public void onEnable() {
        getLogger().info("插件已加载,哔哩哔哩@我就是猪大仙啦");
        saveDefaultConfig();  // 保存默认配置
        Bukkit.getPluginManager().registerEvents(this, this);  // 注册事件监听器
        this.users = new HashMap<>();  // 初始化用户数据
        this.inviteCodes = new HashSet<>();  // 初始化邀请码
        this.getCommand("invite").setTabCompleter(this);  // 设置命令自动补全
        loadData();  // 加载数据
    }

    @Override
    public void onDisable() {
        getLogger().info("插件已卸载,哔哩哔哩@我就是猪大仙啦");
        saveData();  // 保存数据
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = users.get(player.getName());
        if (user == null) {
            user = new User(player.getName());
            users.put(player.getName(), user);
        }

        if (user.isSpectator()) {
            player.sendMessage(ChatColor.YELLOW + "请使用 /invite set <邀请码> 来接受邀请码");
            player.setGameMode(GameMode.SPECTATOR);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        User user = users.get(player.getName());
        if (command.getName().equalsIgnoreCase("invite")) {
            if (args.length == 0) {
                return false;
            }

            if (args[0].equalsIgnoreCase("set")) {
                if (user.isSpectator()) {
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "请输入邀请码");
                        return true;
                    }

                    String inviteCode = args[1];
                    if (!inviteCodes.contains(inviteCode)) {
                        player.sendMessage(ChatColor.RED + "无效的邀请码");
                        return true;
                    }

                    user.setInviteCode(inviteCode);
                    user.setSpectator(false);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    player.sendMessage(ChatColor.GREEN + "你已接受邀请码并被传送到出生点");


                    String newInviteCode = generateInviteCode();
                    user.setOwnInviteCode(newInviteCode);
                    inviteCodes.add(newInviteCode);
                    player.sendMessage(ChatColor.GREEN + "你的新邀请码是: " + newInviteCode);
                } else {
                    player.sendMessage(ChatColor.RED + "你已经接受过邀请了，无法再次接受邀请码");
                }
            } else if (args[0].equalsIgnoreCase("see")) {
                String ownInviteCode = user.getOwnInviteCode();
                if (ownInviteCode == null) {
                    player.sendMessage(ChatColor.RED + "你没有邀请码,请先接受邀请");
                    return true;
                }

                player.sendMessage(ChatColor.GREEN + "你的邀请码是: " + ownInviteCode);
            }

            return true;
        }

        return false;
    }

    private String generateInviteCode() {
        String inviteCode;
        Random random = new Random();
        do {
            inviteCode = String.format("%06d", random.nextInt(900000) + 100000);
        } while (inviteCodes.contains(inviteCode));
        inviteCodes.add(inviteCode);

        // 将新的邀请码添加到配置中
        List<String> configInviteCodes = getConfig().getStringList("invite-codes");
        configInviteCodes.add(inviteCode);
        getConfig().set("invite-codes", configInviteCodes);

        // 将配置保存到文件
        saveConfig();

        return inviteCode;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("invite")) {
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                list.add("set");
                list.add("see");
                return list;
            }
        }
        return null;
    }

    private void saveData() {
        // 将用户数据转换为列表
        List<Map<String, Object>> usersList = new ArrayList<>();
        for (User user : users.values()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("inviteCode", user.getInviteCode());
            userMap.put("ownInviteCode", user.getOwnInviteCode());
            userMap.put("isSpectator", user.isSpectator());
            usersList.add(userMap);
        }

        // 将用户列表和邀请码保存到配置中
        getConfig().set("users", usersList);
        getConfig().set("invite-codes", new ArrayList<>(inviteCodes));

        // 将配置保存到文件
        saveConfig();
    }

    private void loadData() {
        // 从配置中加载用户列表和邀请码
        List<Map<?, ?>> usersList = getConfig().getMapList("users");
        for (Map<?, ?> userMap : usersList) {
            String username = (String) userMap.get("username");
            String inviteCode = (String) userMap.get("inviteCode");
            String ownInviteCode = (String) userMap.get("ownInviteCode");
            boolean isSpectator = (Boolean) userMap.get("isSpectator");

            User user = new User(username);
            user.setInviteCode(inviteCode);
            user.setOwnInviteCode(ownInviteCode);
            user.setSpectator(isSpectator);

            users.put(username, user);

            // 将用户的邀请码添加到邀请码列表中
            if (ownInviteCode != null) {
                inviteCodes.add(ownInviteCode);
            }
        }

        // 加载配置文件中的邀请码列表
        inviteCodes.addAll(getConfig().getStringList("invite-codes"));
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = users.get(player.getName());
        if (user != null && user.isSpectator()) {
            String command = event.getMessage().split(" ")[0];
            if (!command.equalsIgnoreCase("/invite")) {
                player.sendMessage(ChatColor.RED + "请先输入邀请码（输入/invite set <邀请码> 来接受邀请码）");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = users.get(player.getName());
        if (user != null && user.isSpectator()) {
            player.sendMessage(ChatColor.RED + "请先输入邀请码（输入/invite set <邀请码> 来接受邀请码）");
            event.setCancelled(true);
        }
    }
}