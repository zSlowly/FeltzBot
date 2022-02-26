package pt.slowly.redefeltzbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class guildMemberJoin extends ListenerAdapter {
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e){
        
        TextChannel canal = e.getJDA().getTextChannelById("923644117957869599");
        
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(e.getMember().getUser().getAsTag())
                .setDescription("Seja bem vindo <@" + e.getMember().getId() + "> á **Rede Feltz**! Atualmente contamos"
                        + " com a presença de " + e.getGuild().getMemberCount() + " usuários! \nVocê pode ver as nossas regras em <#798680335796928552>. \nAtualizações sobre o nosso servidor em <#798676068517019678>. \nFaça o formulário em <#798677330666848287>. \n \n")
                .setFooter("© Rede Feltz - Todos os direitos reservados", e.getJDA().getSelfUser().getAvatarUrl());
        
        canal.sendMessage(eb.build()).queue();
        
    }
    
}
