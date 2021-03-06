/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command Created: 2012/10/25
 * 13:54:47
 */
package net.syamn.sakuragroup.command;

import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.task.ExpiredCheck;
import net.syamn.utils.Util;
import net.syamn.utils.exception.CommandException;

import org.bukkit.Bukkit;

/**
 * ForceCheckCommand (ForceCheckCommand.java)
 * 
 * @author syam(syamn)
 */
public class ForceCheckCommand extends BaseCommand {
    public ForceCheckCommand() {
        bePlayer = false;
        name = "forcecheck";
        argLength = 0;
        usage = "<- force group expired check";
    }

    @Override
    public void execute() throws CommandException {
        if (ExpiredCheck.isRunning()) {
            throw new CommandException("&c既にチェックタスクが起動しています！");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new ExpiredCheck(plugin, sender));
        
        Util.message(sender, "&aチェックを開始しました");
    }

    @Override
    public boolean permission() {
        return Perms.FORCE_CHECK.has(sender);
    }
}
