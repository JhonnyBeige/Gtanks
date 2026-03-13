package gtanks.discord.commands.Default;

import gtanks.main.params.OnlineStats;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

public class OnlineCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw().toLowerCase();

        if (content.startsWith("en?online") || content.startsWith("ru?online")) {
            CompletableFuture.runAsync(() -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.GREEN);

                String title;

                if (content.startsWith("en?online")) {
                    title = "__**Online players**__";
                    embedBuilder.addField("Current online", String.valueOf(OnlineStats.getOnline()), false);
                    embedBuilder.addField("Max online", String.valueOf(OnlineStats.getMaxOnline()), false);
                    embedBuilder.addField("Players in battles", String.valueOf(OnlineStats.getInBattlesOnline()), false);
                } else {
                    title = "__**Онлайн игроков**__";
                    embedBuilder.addField("Текущий онлайн", String.valueOf(OnlineStats.getOnline()), false);
                    embedBuilder.addField("Максимум онлайн", String.valueOf(OnlineStats.getMaxOnline()), false);
                    embedBuilder.addField("Игроков в битвах", String.valueOf(OnlineStats.getInBattlesOnline()), false);
                }

                embedBuilder.setTitle(title);

                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            });
        }
    }
}
