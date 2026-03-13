package gtanks.discord.commands.Owner;

import gtanks.discord.commands.Permissions;
import gtanks.system.restart.ServerRestartService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RestartCommand extends ListenerAdapter {

    private final ServerRestartService serverRestartService = new ServerRestartService();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw().trim().toLowerCase(Locale.getDefault());

        if (messageContent.startsWith("en?restart")) {
            if (event.getMember() == null || !Permissions.hasPermission(event.getMember())) {
                Permissions.sendNoPermissionMessage(event, "en");
                return;
            }

            serverRestartService.restart();

            startCountdown(event, "en");
        } else if (messageContent.startsWith("ru?restart")) {
            if (event.getMember() == null || !Permissions.hasPermission(event.getMember())) {
                Permissions.sendNoPermissionMessage(event, "ru");
                return;
            }

            serverRestartService.restart();

            startCountdown(event, "ru");
        }
    }

    private void startCountdown(MessageReceivedEvent event, String language) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setTitle(language.equals("en") ? "Restarting Server" : "Перезагрузка сервера");
        embedBuilder.setDescription(language.equals("en") ? "Countdown: 49 seconds remaining..." : "Обратный отсчет: 49 секунд осталось...");
        embedBuilder.setFooter("Restart Command");

        Message message = event.getChannel().sendMessageEmbeds(embedBuilder.build()).complete();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private int count = 49;

            @Override
            public void run() {
                if (count == 0) {
                    sendSuccessEmbed(event, language);
                    timer.cancel();
                } else {
                    String countdownMessage = language.equals("en")
                            ? "Countdown: " + count + " seconds remaining..."
                            : "Обратный отсчет: " + count + " секунд осталось...";

                    EmbedBuilder updatedEmbed = new EmbedBuilder();
                    updatedEmbed.setColor(Color.GREEN);
                    updatedEmbed.setTitle(language.equals("en") ? "Restarting Server" : "Перезагрузка сервера");
                    updatedEmbed.setDescription(countdownMessage);
                    updatedEmbed.setFooter("Restart Command");

                    message.editMessageEmbeds(updatedEmbed.build()).queue();
                    count--;
                }
            }
        }, 0, 1000);
    }

    private void sendSuccessEmbed(MessageReceivedEvent event, String language) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.GREEN);

        String title, description;
        if (language.equals("en")) {
            title = "Server Restarted";
            description = "The server has been successfully restarted.";
        } else {
            title = "Сервер перезапущен";
            description = "Сервер был успешно перезапущен.";
        }

        embedBuilder.setTitle(title);
        embedBuilder.setDescription(description);
        embedBuilder.setFooter("Restart Command");

        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
