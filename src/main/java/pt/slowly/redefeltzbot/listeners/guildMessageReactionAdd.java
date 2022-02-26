package pt.slowly.redefeltzbot.listeners;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pt.slowly.redefeltzbot.Main;

public class guildMessageReactionAdd extends ListenerAdapter {
    
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e){
        
        if (e.getMessageId().equals("925008190817587200")){
            
            Role role = e.getGuild().getRoleById("798673816360452106");
            
            e.getGuild().addRoleToMember(e.getMember(), role).queue();
            
        }
        
        if (e.getChannel().getId().equals("800032677893505024")){
            
            if (e.getUser() == e.getJDA().getSelfUser()) return;
            e.getReaction().removeReaction(e.getUser()).queue();

            if (Main.usersTicket.containsKey(e.getUser())){

                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("TICKET")
                                .setDescription("Parece que você já possui um canal de suporte criado! Por favor utilize o mesmo.")
                                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());

                e.getUser().openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(eb.build()))
                        .complete();

                return;
            }

            TextChannel channel = e.getGuild().createTextChannel("ticket-" + e.getUser().getAsTag()).complete();
            Main.usersTicket.put(e.getUser(), channel);

            List<Role> roles = new ArrayList<>();
            List<Role> rolesDeny = new ArrayList<>();
            roles.add(e.getGuild().getRoleById("798667167771656272"));
            roles.add(e.getGuild().getRoleById("798666937109708840"));
            roles.add(e.getGuild().getRoleById("798666844684156938"));
            roles.add(e.getGuild().getRoleById("798666786261958718"));
            roles.add(e.getGuild().getRoleById("798664896207257600"));
            
            rolesDeny.add(e.getGuild().getRoleById("798673816360452106"));
            rolesDeny.add(e.getGuild().getRoleById("798673737825779753"));
            rolesDeny.add(e.getGuild().getRoleById("812492259852419134"));
            rolesDeny.add(e.getGuild().getRoleById("798673584155131915"));
            rolesDeny.add(e.getGuild().getRoleById("798672729196331058"));
            rolesDeny.add(e.getGuild().getRoleById("798663949255573504"));

            roles.forEach(role -> channel.createPermissionOverride(role)
                .setAllow(Permission.VIEW_CHANNEL)
                .setAllow(Permission.MESSAGE_WRITE)
                .setAllow(Permission.MESSAGE_HISTORY)
                .queue());
            
            rolesDeny.forEach(role -> channel.createPermissionOverride(role)
                .setDeny(Permission.VIEW_CHANNEL)
                .queue());

            channel.createPermissionOverride(e.getMember())
                .setAllow(Permission.VIEW_CHANNEL)
                .setAllow(Permission.MESSAGE_WRITE)
                .setAllow(Permission.MESSAGE_HISTORY)
                .queue();
            
            EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("TICKET")
                        .setDescription("Por favor aguarde um pouco que algum membro da nossa equipe irá ajudá-lo. Não marque ninguém ou poderá atrasar a nossa resposta. \n\nCaso queira fechar o ticket, digite '!fechar'")
                        .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
            
            channel.sendMessage(eb.build()).queue();
            
        }
        
    }
    
}
