package pt.slowly.redefeltzbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import pt.slowly.redefeltzbot.lavaplayer.GuildMusicManager;
import pt.slowly.redefeltzbot.lavaplayer.PlayerManager;
import pt.slowly.redefeltzbot.listeners.guildMemberJoin;
import pt.slowly.redefeltzbot.listeners.guildMessageReactionAdd;
import pt.slowly.redefeltzbot.listeners.messageReceived;
import pt.slowly.redefeltzbot.utils.MySQL;

public class Main {
    
    public static YamlFile database;
    public static List<String> channelsId;
    public static HashMap<User, TextChannel> usersTicket = new HashMap<>();
    
    public static void main(String[] args) throws IOException{
        
        try{
            JDA jda = JDABuilder.createDefault("OTIzNjE5NDg3MDA4MzA5Mjc5.YcSprQ.R2LwnKhKzX9iZwcmdRnC0YV0uco")
                    .addEventListeners(new guildMemberJoin())
                    .addEventListeners(new messageReceived())
                    .addEventListeners(new guildMessageReactionAdd())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
            jda.awaitReady();
            Guild guild = jda.getGuildById("798663949255573504");
            System.out.println("O bot foi inicializado com " + guild.getMemberCount() + " membros no servidor!");
            
            final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            
            new Timer().schedule(new TimerTask(){
                
            @Override
            public void run(){
                if (audioPlayer.getPlayingTrack() == null){
                
                    String[] message = {"com " + guild.getMemberCount() + " usuários.", "em redefeltz.com", "no novo servidor da Rede Feltz"};
                    int rdm = new Random().nextInt(message.length - 1);
                    jda.getPresence().setActivity(Activity.playing(message[rdm]));
                
                }else{
                    jda.getPresence().setActivity(Activity.listening(audioPlayer.getPlayingTrack().getInfo().title));
                }
            }
            },0,30000);
         
        } catch (LoginException | InterruptedException ex){
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //criando arquivo yaml
        final YamlFile yamlFile = new YamlFile("src/resources/database.yml");

        try{

            if (!yamlFile.exists()){
                System.out.println("Database criada com sucesso!");
                yamlFile.createNewFile(true); //criou database
            }else{
                System.out.println("Database carregada com sucesso!");
            }

            yamlFile.load(); //carregou database
            database = yamlFile;

        } catch (IOException | InvalidConfigurationException a){
        }
        
        if (database.contains("Tags")){
            channelsId = database.getStringList("Tags");
        }else{
            channelsId = new ArrayList<>();
        }
        
        if (!database.contains("MySQL")){
            database.set("MySQL.IP", "localhost");
            database.set("MySQL.Porta", 3306);
            database.set("MySQL.Usuario", "root");
            database.set("MySQL.Senha", "");
            database.set("MySQL.DataBase", "permissoes");
            database.save();
        }
        
        MySQL.abrirConexao();
    }
}
