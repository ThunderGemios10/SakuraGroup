/**
 * SakuraGroup - Package: net.syamn.sakuragroup.listener Created: 2012/10/16
 * 5:44:45
 */
package net.syamn.sakuragroup.listener;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.manager.SignManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.utils.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * SignListener (SignListener.java)
 * 
 * @author syam(syamn)
 */
public class SignListener implements Listener {
    private static final String msgPrefix = SakuraGroup.msgPrefix;

    private final SakuraGroup plugin;

    public SignListener(final SakuraGroup plugin) {
        this.plugin = plugin;
    }

    // プレイヤーがクリックした
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Block block = event.getClickedBlock();

        // ブロックをクリックしていなければ返す
        if (block == null) {
            return;
        }

        final int id = block.getTypeId();
        if (id == Material.SIGN_POST.getId() || id == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).equals("§a[SakuraGroup]")) {
                final Player player = event.getPlayer();
                if (!Perms.CHANGE_SIGN.has(player)) {
                    return;
                }

                Group group = null;
                for (String name : plugin.getPEXmgr().getAvailables()) {
                    if (name.equalsIgnoreCase(sign.getLine(1))) {
                        group = plugin.getPEXmgr().getGroupExact(name);
                        break;
                    }
                }
                if (group == null) {
                    Util.message(player, msgPrefix + "&6このグループは存在しません！");
                    return;
                }

                SignManager.setSelectedGroup(player, group);
                Util.message(player, msgPrefix + "&aグループ " + group.getColor() + group.getName() + " &aを選択しました！");
                Util.message(player, msgPrefix + "&6/group change &aコマンドでグループを変更します！");

                event.setCancelled(true);
            }
        }
    }

    // 看板を設置した
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final BlockState state = event.getBlock().getState();

        if (state instanceof Sign) {
            Sign sign = (Sign) state;

            /* [SakuraGroup] 特殊看板 */
            if (event.getLine(0).toLowerCase().indexOf("[sakuragroup]") != -1) {
                // 権限チェック
                if (!Perms.PLACESIGN.has(player)) {
                    event.setLine(0, "§c[SakuraGroup]");
                    event.setLine(1, "Perm Denied :(");
                    Util.message(player, "&6You don't have permission to use this!");
                    return;
                }

                // 内容チェック
                Group group = null;
                for (String name : plugin.getPEXmgr().getAvailables()) {
                    if (name.equalsIgnoreCase(event.getLine(1))) {
                        group = plugin.getPEXmgr().getGroupExact(name);
                        break;
                    }
                }

                // 1行目の文字色
                if (group == null) {
                    event.setLine(0, "§c[SakuraGroup]");
                    Util.message(player, "&cグループが見つかりません！");
                } else {
                    if (group.getName().length() > 15) {
                        event.setLine(0, "§c[SakuraGroup]");
                        Util.message(player, "&cグループ名が15文字を超えているため看板にできません！");
                        return;
                    }
                    event.setLine(0, "§a[SakuraGroup]");
                    event.setLine(1, group.getName());

                    Util.message(player, "&aグループ変更看板を設置しました！");
                }
            }
        }
    }
}
