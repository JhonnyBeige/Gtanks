package gtanks.discord.commands.Owner;

import gtanks.discord.commands.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HelpCommand extends ListenerAdapter {
    private final Map<String, String> englishCommands = new HashMap<>();
    private final Map<String, String> russianCommands = new HashMap<>();

    public HelpCommand() {
        englishCommands.put("link", "Gives a link to the discord server.");
        englishCommands.put("en?restart", "Restarts the server.");
        englishCommands.put("en?online", "Displays the number of online players and their details.");
        englishCommands.put("en?clear", "Clears the chat room.");
        englishCommands.put("en?help", "Displays this help message.");

        russianCommands.put("ссылка", "Дает ссылку на дискорд сервер.");
        russianCommands.put("ru?restart", "Производит рестарт сервера.");
        russianCommands.put("ru?online", "Отображает количество онлайн игроков и их данные.");
        russianCommands.put("ru?clear", "Очищает чат.");
        russianCommands.put("ru?help", "Отображает это сообщение справки.");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.getMember() != null && !Permissions.hasPermission(event.getMember())) {
            return;
        }

        String content = event.getMessage().getContentRaw().trim().toLowerCase(Locale.ROOT);

        if (content.startsWith("en?help")) {
            sendHelpMessage(event, "English");
        } else if (content.startsWith("ru?help")) {
            sendHelpMessage(event, "Russian");
        }
    }

    private void sendHelpMessage(MessageReceivedEvent event, String language) {
        Map<String, String> commands = language.equalsIgnoreCase("English") ? englishCommands : russianCommands;
        EmbedBuilder embedBuilder = new EmbedBuilder();

        String title = language.equalsIgnoreCase("English") ? "Command List" : "Список Команд";
        embedBuilder.setTitle(title)
                .setColor(Color.GREEN);

        for (Map.Entry<String, String> entry : commands.entrySet()) {
            embedBuilder.addField(entry.getKey(), entry.getValue(), false);
        }

        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
