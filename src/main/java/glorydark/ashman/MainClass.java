package glorydark.ashman;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MainClass extends PluginBase {

    public Map<String, Object> lang;

    public String path;

    public Integer time = 0;

    public Integer maxTime = 6000;

    @Override
    public void onLoad() {
        this.getLogger().info("AshMan onLoad!");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        path = this.getDataFolder().getPath();
        new File(path+"/languages/").mkdirs();
        saveResource("languages/zh-cn.yml", false);
        Config config = new Config(path+"/config.yml", Config.YAML);
        String language = config.getString("language", "zh-cn");
        lang = new Config(path+"/languages/"+language+".yml", Config.YAML).getAll();
        maxTime = config.getInt("clean-intervals", 300);
        Server.getInstance().getScheduler().scheduleRepeatingTask(()->{
            time++;
            if(time + 5 >= maxTime){
                this.getServer().broadcastMessage(this.getTranslation("specific-second-last").replace("%d", String.valueOf(maxTime - time)));
            }
            if(time.equals(maxTime)){
                cleanAllEntities();
                time = 0;
            }
        }, 20);
        this.getLogger().info("AshMan onEnabled! Author: Glorydark");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("AshMan onDisabled!");
    }

    public String getTranslation(String key){
        return (String) lang.getOrDefault(key, "null");
    }

    public void cleanAllEntities(){
        int mobs = 0;
        int drops = 0;
        Config config = new Config(path+"/config.yml", Config.YAML);
        List<String> levels = config.getStringList("no-clean-levels");
        for(Level level: Server.getInstance().getLevels().values()) {
            if (!levels.contains(level.getName())) {
                for (Entity entity : level.getEntities()) {
                    if (!(entity instanceof EntityHuman)) {
                        if (entity instanceof EntityItem) {
                            drops += 1;
                        } else {
                            mobs += 1;
                        }
                        level.removeEntity(entity);
                    }
                }
            }
        }
        this.getServer().broadcastMessage(this.getTranslation("clean-message").replace("%d1", String.valueOf(mobs)).replace("%d2", String.valueOf(drops)));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if((!(sender instanceof Player)) || sender.isOp()){
            cleanAllEntities();
            time = 0;
        }else{
            this.getLogger().info(this.getTranslation("no-permission"));
        }
        return true;
    }
}
