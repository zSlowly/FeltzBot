package pt.slowly.redefeltzbot.listeners;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import pt.slowly.redefeltzbot.Main;
import pt.slowly.redefeltzbot.lavaplayer.GuildMusicManager;
import pt.slowly.redefeltzbot.lavaplayer.PlayerManager;
import pt.slowly.redefeltzbot.utils.MySQL;

public class messageReceived extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        
        if (e.getMember().getUser() != e.getJDA().getSelfUser() && Main.database.contains(e.getMember().getId())){
            long tempo = Main.database.getLong(e.getMember().getId() + ".tempo") * 60000;
            long agora = Main.database.getLong(e.getMember().getId() + ".agora");
            long diferenca = Calendar.getInstance().getTimeInMillis() - agora;
            
            if (diferenca <= tempo){
                e.getMember().getUser().openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage("Você está silenciado e por isso não pode falar nos chats!"))
                        .complete();
                e.getMessage().delete().complete();
            }else{
                Main.database.set(e.getMember().getId(), null);
                try{
                    Main.database.save();
                } catch (IOException ex){
                    Logger.getLogger(messageReceived.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (e.getMessage().getContentRaw().startsWith("!fechar")){
            
            if (e.getMessage().getChannel().getName().contains("ticket")){

                for (Map.Entry<User, TextChannel> channel : Main.usersTicket.entrySet()){
                    if (channel.getValue().equals(e.getChannel())){
                        Main.usersTicket.remove(channel.getKey());
                    }
                }

                e.getGuild().getGuildChannelById(e.getChannel().getId()).delete().queue();
            }
            
        }
        
        if (e.getMessage().getContentRaw().startsWith("!ticket-msg")){
            
            Role role = e.getGuild().getRoleById("798664896207257600");
            
            if (e.getMember().getRoles().contains(role)){
                
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("TICKET")
                        .setDescription("Aqui você pode tirar todo o tipo de dúvidas em relação ao nosso servidor. \nPara abrir um ticket basta reagir abaixo.")
                        .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                
                e.getChannel().sendMessage(eb.build()).queue(msg -> msg.addReaction("🎫").queue());
                e.getMessage().delete().queue();
                
            }
        }
        
        if (e.getMessage().getContentRaw().startsWith("!limpar")){
            
            Role role = e.getGuild().getRoleById("798664896207257600");
            
            if (e.getMember().getRoles().contains(role)){
                
                String[] args = e.getMessage().getContentRaw().split(" ");
            
                if (args.length != 2){
                    e.getMessage().reply("Digite !limpar <quantia de mensagens>").queue();
                    return;
                }
                
                int qnt = Integer.parseInt(args[1]);
                
                if (qnt > 100){
                    e.getMessage().reply("Você não pode deletar mais de 100 mensagens.").queue();
                    return;
                }
                
                e.getChannel().purgeMessages(e.getChannel().getHistory().retrievePast(qnt).complete());
                e.getMessage().reply("Foram deletadas " + qnt + " mensagens!").queue();
                        
                
            }else{
                e.getMessage().reply("Você não possui permissão para executar este comando.").queue();
            }
            
        }
        
        if (e.getMessage().getContentRaw().startsWith("!solicitar-tag")){
            
            String[] args = e.getMessage().getContentRaw().split(" ");
            
            if (args.length != 4){
                e.getMessage().reply("Digite !solicitar-tag <mini-yt/yt/yt+> <link do vídeo> <nick in-game>").queue();
                return;
            }
            
            try
            {
                
                String videoTry = args[2].split("v=")[1];
                Video video = null;
                if (videoTry.contains("&")){
                    video = getVideoInformation(videoTry.split("&")[0]);
                }else{
                    video = getVideoInformation(videoTry);
                }
                Channel canal = getChannelInformation(video.getSnippet().getChannelId());
                
                if (args[1].equalsIgnoreCase("mini-yt")){
                    
                    if (e.getMember().getRoles().contains(e.getGuild().getRoleById("798672614984777728"))){
                        e.getMessage().reply("Você já possui a tag MiniYT!").queue();
                    }
                    
                    if (Main.channelsId.contains(video.getSnippet().getChannelId())){
                        e.getMessage().reply("Alguém já solicitou a tag por este canal!").queue();
                        return;
                    }
                    
                    if (Calendar.getInstance().getTimeInMillis() - video.getSnippet().getPublishedAt().getValue() > 8.64e+7){
                        e.getMessage().reply("Este vídeo já foi postado há mais de 24 horas!").queue();
                        return;
                    }
                    
                    if (!video.getSnippet().getTitle().toUpperCase().contains("FELTZ")){
                        e.getMessage().reply("O título deste vídeo não contem a palavra 'Feltz'").queue();
                        return;
                    }
                    
                    if (canal.getStatistics().getSubscriberCount().intValue() < 25){
                        e.getMessage().reply("O seu canal possui menos de 25 inscritos!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 15){
                        e.getMessage().reply("O vídeo possui menos de 15 visualizações!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 5){
                        e.getMessage().reply("O vídeo possui menos de 5 likes!").queue();
                        return;
                    }
                    
                    if (video.getSnippet().getDescription().toUpperCase().contains("IP: REDEFELTZ.COM") || video.getSnippet().getDescription().toUpperCase().contains("LOJA: LOJA.REDEFELTZ.COM") || video.getSnippet().getDescription().toUpperCase().contains("NICKNAME: " + args[3].toUpperCase())){
                        e.getMessage().reply("Faltam algum dos seguintes parâmetros no seu vídeo: \nIp: redefeltz.com \nLoja: loja.redefeltz.com \nNickname: " + args[3]).queue();
                        return;
                    }
                    
                    Main.channelsId.add(video.getSnippet().getChannelId());
                    Main.database.set("Tags", Main.channelsId);
                    Main.database.save();
                    e.getMessage().reply("O canal e o vídeo cumprem todos os requisitos. A tag Mini-YT foi setada!").queue();
                    e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById("798672614984777728")).queue();
                    MySQL.setTag(args[3], args[1]);
                    
                }
                
                if (args[1].equalsIgnoreCase("yt")){
                    
                    if (e.getMember().getRoles().contains(e.getGuild().getRoleById("798672504259870790"))){
                        e.getMessage().reply("Você já possui a tag YT!").queue();
                    }
                    
                    if (Main.channelsId.contains(video.getSnippet().getChannelId())){
                        e.getMessage().reply("Alguém já solicitou a tag por este canal!").queue();
                        return;
                    }
                    
                    if (Calendar.getInstance().getTimeInMillis() - video.getSnippet().getPublishedAt().getValue() > 8.64e+7){
                        e.getMessage().reply("Este vídeo já foi postado há mais de 24 horas!").queue();
                        return;
                    }
                    
                    if (!video.getSnippet().getTitle().toUpperCase().contains("FELTZ")){
                        e.getMessage().reply("O título deste vídeo não contem a palavra 'Feltz'").queue();
                        return;
                    }
                    
                    if (canal.getStatistics().getSubscriberCount().intValue() < 75){
                        e.getMessage().reply("O seu canal possui menos de 75 inscritos!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 45){
                        e.getMessage().reply("O vídeo possui menos de 45 visualizações!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 10){
                        e.getMessage().reply("O vídeo possui menos de 10 likes!").queue();
                        return;
                    }
                    
                    if (!video.getSnippet().getDescription().toUpperCase().contains("IP: REDEFELTZ.COM") || !video.getSnippet().getDescription().toUpperCase().contains("LOJA: LOJA.REDEFELTZ.COM") || !video.getSnippet().getDescription().toUpperCase().contains("NICKNAME: " + args[3].toUpperCase())){
                        e.getMessage().reply("Faltam algum dos seguintes parâmetros no seu vídeo: \nIp: redefeltz.com \nLoja: loja.redefeltz.com \nNickname: " + args[3]).queue();
                        return;
                    }
                    
                    Main.channelsId.add(video.getSnippet().getChannelId());
                    Main.database.set("Tags", Main.channelsId);
                    Main.database.save();
                    e.getMessage().reply("O canal e o vídeo cumprem todos os requisitos. A tag YT foi setada!").queue();
                    e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById("798672504259870790")).queue();
                    MySQL.setTag(args[3], args[1]);
                    
                }
                
                if (args[1].equalsIgnoreCase("yt+")){
                    
                    if (e.getMember().getRoles().contains(e.getGuild().getRoleById("798672429605978142"))){
                        e.getMessage().reply("Você já possui a tag YT+!").queue();
                    }
                    
                    if (Main.channelsId.contains(video.getSnippet().getChannelId())){
                        e.getMessage().reply("Alguém já solicitou a tag por este canal!").queue();
                        return;
                    }
                    
                    if (Calendar.getInstance().getTimeInMillis() - video.getSnippet().getPublishedAt().getValue() > 8.64e+7){
                        e.getMessage().reply("Este vídeo já foi postado há mais de 24 horas!").queue();
                        return;
                    }
                    
                    if (!video.getSnippet().getTitle().toUpperCase().contains("FELTZ")){
                        e.getMessage().reply("O título deste vídeo não contem a palavra 'Feltz'").queue();
                        return;
                    }
                    
                    if (canal.getStatistics().getSubscriberCount().intValue() < 150){
                        e.getMessage().reply("O seu canal possui menos de 150 inscritos!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 75){
                        e.getMessage().reply("O vídeo possui menos de 75 visualizações!").queue();
                        return;
                    }
                    
                    if (video.getStatistics().getViewCount().intValue() < 15){
                        e.getMessage().reply("O vídeo possui menos de 15 likes!").queue();
                        return;
                    }
                    
                    if (!video.getSnippet().getDescription().toUpperCase().contains("IP: REDEFELTZ.COM") || !video.getSnippet().getDescription().toUpperCase().contains("LOJA: LOJA.REDEFELTZ.COM") || !video.getSnippet().getDescription().toUpperCase().contains("NICKNAME: " + args[3].toUpperCase())){
                        e.getMessage().reply("Faltam algum dos seguintes parâmetros no seu vídeo: \nIp: redefeltz.com \nLoja: loja.redefeltz.com \nNickname: " + args[3]).queue();
                        return;
                    }
                    
                    Main.channelsId.add(video.getSnippet().getChannelId());
                    Main.database.set("Tags", Main.channelsId);
                    Main.database.save();
                    e.getMessage().reply("O canal e o vídeo cumprem todos os requisitos. A tag YT+ foi setada!").queue();
                    e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById("798672429605978142")).queue();
                    MySQL.setTag(args[3], args[1]);
                    
                }
                
            } catch (IOException ex)
            {
                Logger.getLogger(messageReceived.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (e.getMessage().getContentRaw().startsWith("!ouvir")){
            
            String[] args = e.getMessage().getContentRaw().split(" ");
            
            if (args.length != 2){
                e.getMessage().reply("Digite !ouvir <link do youtube>").queue();
                return;
            }
            
            if (!e.getMember().getVoiceState().inVoiceChannel()){
                e.getMessage().reply("Você precisa estar num canal de voz para que isto funcione.").queue();
                return;
            }
            
            AudioManager audioManager = e.getGuild().getAudioManager();
            audioManager.openAudioConnection(e.getMember().getVoiceState().getChannel());
            
            String link = String.join(" ", args[1]);
            
            if (!isUrl(link)){
                link = "ytsearch:" + link;
            }

            PlayerManager.getInstance().loadAndPlay(e.getMessage().getTextChannel(), link);
       
        }
        
        if (e.getMessage().getContentRaw().startsWith("!parar")){
            
            String[] args = e.getMessage().getContentRaw().split(" ");
            
            if (args.length != 1){
                e.getMessage().reply("Digite !parar").queue();
                return;
            }
            
            if (!e.getMember().getVoiceState().inVoiceChannel()){
                e.getMessage().reply("Você precisa estar num canal de voz para que isto funcione.").queue();
                return;
            }
            
            AudioManager audioManager = e.getGuild().getAudioManager();
            
            if (!audioManager.isConnected()){
                e.getMessage().reply("Eu já não estou a tocar música!").queue();
                return;
            }
            
            if (audioManager.getConnectedChannel() != e.getMember().getVoiceState().getChannel()){
                e.getMessage().reply("Eu não estou no mesmo canal de você!").queue();
                return;
            }
            
            final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(e.getMessage().getGuild());
            
            musicManager.scheduler.player.stopTrack();
            musicManager.scheduler.queue.clear();
            audioManager.closeAudioConnection();
            e.getMessage().reply("Você parou de ouvir música!").queue();
            
        }
        
        if (e.getMessage().getContentRaw().startsWith("!pular")){
            
            String[] args = e.getMessage().getContentRaw().split(" ");
            
            if (args.length != 1){
                e.getMessage().reply("Digite !parar").queue();
                return;
            }
            
            if (!e.getMember().getVoiceState().inVoiceChannel()){
                e.getMessage().reply("Você precisa estar num canal de voz para que isto funcione.").queue();
                return;
            }
            
            AudioManager audioManager = e.getGuild().getAudioManager();
            
            if (!audioManager.isConnected()){
                e.getMessage().reply("Eu já não estou a tocar música!").queue();
                return;
            }
            
            if (audioManager.getConnectedChannel() != e.getMember().getVoiceState().getChannel()){
                e.getMessage().reply("Eu não estou no mesmo canal de você!").queue();
                return;
            }
            
            final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(e.getMessage().getGuild());
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            
            if (audioPlayer.getPlayingTrack() == null){
                e.getMessage().reply("Eu já não estou a tocar música!").queue();
                return;
            }
            
            musicManager.scheduler.nextTrack();
            e.getMessage().reply("Você pulou a música.").queue();
        }
        
        if (e.getMessage().getContentRaw().startsWith("!verificação")){
            
            Role role = e.getGuild().getRoleById("798664896207257600");
            
            if (e.getMember().getRoles().contains(role)){
                
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("VERIFICAÇÃO")
                        .setDescription("Reaga abaixo para ter acesso aos canais de bate-papo do servidor. \nAo reagir concorda com todas as regras do servidor de discord e caso quebre alguma, será responsabilizado pelos seus atos e punido conforme as regras.");
                
                e.getChannel().sendMessage(eb.build()).queue(message -> {
                    message.addReaction("✔️").queue();
                });
                e.getMessage().delete().queue();
                
            }else{
                e.getMessage().reply("Você não possui permissão para executar este comando.").queue();
            }
            
        }
        
        if (e.getMessage().getContentDisplay().startsWith("!banir")){
        
            Role role = e.getGuild().getRoleById("798664896207257600");
            TextChannel canal = e.getGuild().getTextChannelById("800790072282316821");
            
            if (e.getMember().getRoles().contains(role)){
                
                if (e.getMessage().getContentRaw().split(" ").length >= 3){
                    
                    User user = e.getJDA().retrieveUserById(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "")).complete();
                    StringBuilder motivo = new StringBuilder();
                    
                    for (int i = 2; i < e.getMessage().getContentRaw().split(" ").length; i++){
                        motivo.append(e.getMessage().getContentRaw().split(" ")[i]).append(" ");
                    }

                    EmbedBuilder eb = new EmbedBuilder()
                                .setTitle("VOCÊ FOI PUNIDO!")
                                .setDescription("Você foi punido da Rede Feltz! \nMotivo: " + motivo + " \nAutor: " + e.getAuthor().getName())
                                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                        user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(eb.build()))
                        .complete();
                        
                    EmbedBuilder eb2 = new EmbedBuilder()
                                .setTitle("O JOGADOR " + user.getName() + " FOI PUNIDO!")
                                .setDescription("O jogador " + user.getName() + " foi punido da Rede Feltz! \nMotivo: " + motivo + " \nAutor: " + e.getAuthor().getName())
                                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                    canal.sendMessage(eb2.build()).complete();
                    
                    e.getGuild().ban(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", ""), 0).queue();
                    
                    e.getMessage().reply("Usuário banido com sucesso!").queue();
                    
                }else{
                
                    if (e.getMessage().getContentRaw().split(" ").length != 2){
                        e.getMessage().reply("Digite !banir <usuário>").queue();
                    }else{

                        User user = e.getJDA().retrieveUserById(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "")).complete();
                        
                        EmbedBuilder eb = new EmbedBuilder()
                                .setTitle("VOCÊ FOI PUNIDO!")
                                .setDescription("Você foi punido da Rede Feltz! \nMotivo: Sem motivo \nAutor: " + e.getAuthor().getName())
                                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                        user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(eb.build()))
                        .complete();
                        
                        EmbedBuilder eb2 = new EmbedBuilder()
                                .setTitle("O JOGADOR " + user.getName() + " FOI PUNIDO!")
                                .setDescription("O jogador " + user.getName() + " foi punido da Rede Feltz! \nMotivo: Sem motivo \nAutor: " + e.getAuthor().getName())
                                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                        canal.sendMessage(eb2.build()).complete();
                        
                        e.getGuild().ban(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", ""), 0).queue();
                    
                        e.getMessage().reply("Usuário banido com sucesso!").queue();
                    }
                }
                
            }else{
                e.getMessage().reply("Você não possui permissão para executar este comando.").queue();
            }
            
        }
        
        if (e.getMessage().getContentDisplay().startsWith("!silenciar")){
            
            Role role = e.getGuild().getRoleById("798664896207257600");
            TextChannel canal = e.getGuild().getTextChannelById("800790072282316821");
            
            if (e.getMember().getRoles().contains(role)){
                
                if (e.getMessage().getContentRaw().split(" ").length >= 4){
                    
                    StringBuilder motivo = new StringBuilder();

                    for (int i = 3; i < e.getMessage().getContentRaw().split(" ").length; i++){
                        motivo.append(e.getMessage().getContentRaw().split(" ")[i]).append(" ");
                    }
                    
                    String userId = e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "");

                    if (Main.database.contains(userId)){
                        e.getMessage().reply("Este usuário já está silenciado!").queue();
                    }else{

                        String time1 = e.getMessage().getContentRaw().split(" ")[2];
                        User user = e.getJDA().retrieveUserById(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "")).complete();

                        if (!isNumeric(time1)){
                            e.getMessage().reply("Digite um número inteiro (minutos).").queue();
                            return;
                        }

                        long time = Long.parseLong(time1);

                        Main.database.set(userId + ".tempo", time);
                        Main.database.set(userId + ".agora", Calendar.getInstance().getTimeInMillis());
                        try{
                            Main.database.save();
                            e.getMessage().reply("Usuário silenciado com sucesso!").queue();
                            
                            EmbedBuilder eb = new EmbedBuilder()
                                    .setTitle("VOCÊ FOI SILENCIADO!")
                                    .setDescription("Você foi silenciado da Rede Feltz! \nMotivo: " + motivo + " \nAutor: " + e.getAuthor().getName())
                                    .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());

                            user.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage(eb.build()))
                            .complete();

                        EmbedBuilder eb2 = new EmbedBuilder()
                                    .setTitle("O JOGADOR " + user.getName() + " FOI SILENCIADO!")
                                    .setDescription("O jogador " + user.getName() + " foi silenciado da Rede Feltz! \nMotivo: " + motivo + " \nAutor: " + e.getAuthor().getName())
                                    .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                        canal.sendMessage(eb2.build()).queue();
                            
                        } catch (IOException ex){
                            Logger.getLogger(messageReceived.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                }else{
                    
                    if (e.getMessage().getContentRaw().split(" ").length != 3){
                        e.getMessage().reply("Digite !silenciar <usuário> <tempo em minutos>").queue();
                        return;
                    }
                
                    String userId = e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "");

                    if (Main.database.contains(userId)){
                        e.getMessage().reply("Este usuário já está silenciado!").queue();
                    }else{

                        String time1 = e.getMessage().getContentRaw().split(" ")[2];
                        User user = e.getJDA().retrieveUserById(e.getMessage().getContentRaw().split("<")[1].split(" ")[0].replace(">", "").replace("@!", "")).complete();

                        if (!isNumeric(time1)){
                            e.getMessage().reply("Digite um número inteiro (minutos).").queue();
                            return;
                        }

                        long time = Long.parseLong(time1);

                        Main.database.set(userId + ".tempo", time);
                        Main.database.set(userId + ".agora", Calendar.getInstance().getTimeInMillis());
                        try{
                            Main.database.save();
                            e.getMessage().reply("Usuário silenciado com sucesso!").queue();
                            
                            EmbedBuilder eb = new EmbedBuilder()
                                    .setTitle("VOCÊ FOI SILENCIADO!")
                                    .setDescription("Você foi silenciado da Rede Feltz! \nMotivo: Sem motivo \nAutor: " + e.getAuthor().getName())
                                    .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());

                            user.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage(eb.build()))
                            .complete();

                        EmbedBuilder eb2 = new EmbedBuilder()
                                    .setTitle("O JOGADOR " + user.getName() + " FOI SILENCIADO!")
                                    .setDescription("O jogador " + user.getName() + " foi silenciado da Rede Feltz! \nMotivo: Sem motivo \nAutor: " + e.getAuthor().getName())
                                    .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
                        
                        canal.sendMessage(eb2.build()).queue();
                            
                        } catch (IOException ex){
                            Logger.getLogger(messageReceived.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
            }else{
                e.getMessage().reply("Você não possui permissão para executar este comando.").queue();
            }
            
        }
        
    }
    
    private boolean isNumeric(String string) {
        String regex = "[0-9]+[\\.]?[0-9]*";
        return Pattern.matches(regex, string);
    }
    
    private boolean isUrl(String url){
        try{
            new URI(url);
            return true;
        }catch (URISyntaxException e){
            return false;
        }
    }
    
    private Video getVideoInformation(String id) throws IOException{
        
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
        new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("video-test").build();

        final String videoId = id;
        YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails");
        videoRequest.setId(videoId);
        videoRequest.setKey("AIzaSyCXWPfksEgbPZ3hlUJ69JmpdS0lbsxtBEI");
        VideoListResponse listResponse = videoRequest.execute();
        List<Video> videoList = listResponse.getItems();

        Video targetVideo = videoList.iterator().next();
        
        return targetVideo;
        
    }
    
    private Channel getChannelInformation(String id) throws IOException{
        
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
        new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("video-test").build();

        final String channelId = id;
        YouTube.Channels.List channelRequest = youtube.channels().list("snippet,statistics,contentDetails");
        channelRequest.setId(channelId);
        channelRequest.setKey("AIzaSyCXWPfksEgbPZ3hlUJ69JmpdS0lbsxtBEI");
        ChannelListResponse listResponse = channelRequest.execute();
        List<Channel> channelList = listResponse.getItems();
        
        Channel targetChannel = channelList.iterator().next();
        
        return targetChannel;


    }
          
}
