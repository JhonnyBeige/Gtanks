package gtanks.discord.commands.Moderator;

import gtanks.discord.commands.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClearChatCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw().trim().toLowerCase(Locale.getDefault());

        if (content.startsWith("en?clear")) {
            handleClearCommand(event, content, "en");
        } else if (content.startsWith("ru?clear")) {
            handleClearCommand(event, content, "ru");
        }
    }

    private void handleClearCommand(MessageReceivedEvent event, String content, String language) {
        String[] args = content.replaceFirst(language + "\\?clear", "").trim().split("\\s+");

        MessageChannel channel = event.getChannel();

        if (!(channel instanceof TextChannel)) {
            return;
        }

        TextChannel textChannel = (TextChannel) channel;

        if (!content.startsWith(language + "?clear")) {
            return;
        }

        if (event.getMember() == null || !Permissions.hasPermission(event.getMember())) {
            Permissions.sendNoPermissionMessage(event, language);
            return;
        }

        if (args.length != 1 || !args[0].matches("\\d+")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(language.equals("en") ? "Invalid usage" : "Недопустимое использование")
                    .setDescription(language.equals("en") ? "Correct usage: `en?clear <number>`" : "Правильное использование: `ru?clear <number>`")
                    .setColor(Color.RED);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        int count = Integer.parseInt(args[0]);
        if (count <= 0 || count > 100) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(language.equals("en") ? "Invalid count" : "Недопустимое количество")
                    .setDescription(language.equals("en") ? "You can only delete between 1 and 100 messages at once." : "Одновременно можно удалить от 1 до 100 сообщений.")
                    .setColor(Color.RED);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        textChannel.getHistory().retrievePast(count).queue(messages -> {
            textChannel.purgeMessages(messages);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(language.equals("en") ? "Messages Deleted" : "Сообщения удалены")
                    .setDescription(language.equals("en") ? "Deleted " + count + " messages." : "Удалено " + count + " сообщений.")
                    .setColor(Color.YELLOW);
            event.getChannel().sendMessageEmbeds(embed.build()).queue(response ->
                    response.delete().queueAfter(5, TimeUnit.SECONDS)
            );
        });
    }
}
