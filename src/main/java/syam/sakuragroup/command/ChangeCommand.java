/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/17 7:41:13
 */
package syam.sakuragroup.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.database.Database;
import syam.sakuragroup.exception.CommandException;
import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * ChangeCommand (ChangeCommand.java)
 * @author syam(syamn)
 */
public class ChangeCommand extends BaseCommand{
	public ChangeCommand(){
		bePlayer = true;
		name = "change";
		argLength = 0;
		usage = "<- change your current group";
	}

	@Override
	public void execute() throws CommandException {
		PEXManager mgr = plugin.getPEXmgr();

		// 引数が1つ グループ指定
		if (args.size() >= 1){
			// 新グループ確定
			String newGroup = null;
			for (String groups : mgr.getAvailables()){
				if (groups.equalsIgnoreCase(args.get(0))){
					newGroup = groups; break;
				}
			}
			if (newGroup == null){
				throw new CommandException("指定したグループは存在しません！");
			}

			// prepare update
			//int playerID = -1;
			int status = 0;
			int changed = 0;
			Long unixtime = Util.getCurrentUnixSec();

			// Get Database
			Database db = SakuraGroup.getDatabases();
			HashMap<Integer, ArrayList<String>> result =
					db.read("SELECT `player_id`, `group`, `status`, `changed`, `lastchange` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", player.getName());
			if (result.size() > 0){
				// 既にDB登録済み チェック
				ArrayList<String> record = result.get(1);

				//playerID = Integer.valueOf(record.get(0));
				String currentGroup = record.get(1);
				status = Integer.valueOf(record.get(2));
				changed = Integer.valueOf(record.get(3));
				Long changedTime = Long.valueOf(record.get(4));

				// グループチェック
				if (newGroup.equalsIgnoreCase(currentGroup)){
					throw new CommandException("既に同じグループに所属しています！");
				}

				// ステータスチェック
				if (status != 0){
					throw new CommandException("あなたはグループの変更を禁止されています！");
				}

				// 時間チェック
				Calendar time = Calendar.getInstance();
				time.setTime(Util.getDateByUnixTime(changedTime));
				time.add(plugin.getConfigs().getMeasure(), plugin.getConfigs().getTime());
				if (!time.before(Calendar.getInstance())){
					throw new CommandException("あなたはまだグループの変更可能時間に達していません！");
				}
			}

			// Pay cost
			double cost = plugin.getConfigs().getGroupCost(newGroup);
			boolean paid = false;
			if (plugin.getConfigs().getUseVault() && cost < 0){
				log.warning(logPrefix + "Group " + newGroup + " cost config NOT exist or negative value! Change to 0.");
				cost = 0.0D;
			}
			if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)){
				paid = Actions.takeMoney(player.getName(), cost);
				if (!paid){
					throw new CommandException("&cお金が足りません！ " + Actions.getCurrencyString(cost) + "必要です！");
				}
			}

			// Update!
			db.write("REPLACE INTO " + db.getTablePrefix() + "users (`player_name`, `group`, `status`, `changed`, `lastchange`) " +
					"VALUES (?, ?, ?, ?, ?)", player.getName(), newGroup, status, changed + 1, unixtime.intValue());

			// Change group!
			mgr.changeGroup(player.getName(), newGroup, null);

			// messaging
			//PermissionGroup group = mgr.getPEXgroup(newGroup);
			Group group = mgr.getGroup(newGroup);
			Actions.broadcastMessage(msgPrefix+ "&6" + player.getName() + "&aさんが&f" + group.getColor() + group.getName() + "&aグループに所属しました！");

			String msg = msgPrefix + "&aあなたのグループを変更しました！";
			if (paid) msg = msg + " &c(-" + Actions.getCurrencyString(cost) + ")";
			Actions.message(player, msg);
		}else{
			throw new CommandException("使い方を誤っています！");
		}
	}


	@Override
	public boolean permission() {
		return Perms.CHANGE.has(sender);
	}
}
