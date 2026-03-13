package gtanks.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class Permissions {

    private static final String ALLOWED_ROLE_ID = "1199603881596944504";

    public static boolean hasPermission(Member member) {
        return member.getRoles().stream().anyMatch(role -> role.getId().equals(ALLOWED_ROLE_ID));
    }

    public static void sendNoPermissionMessage(MessageReceivedEvent event, String language) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = language.equalsIgnoreCase("en") ? "Permission Error" : "Ошибка доступа";
        String message = language.equalsIgnoreCase("en") ? "You do not have permission to use this command." : "У вас нет прав на использование этой команды.";
        embedBuilder.setTitle(title)
                .setDescription(message)
                .setColor(Color.RED);

        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
