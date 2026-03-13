package gtanks.discord.commands.Default;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordLinkCommand extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().toLowerCase();

        if (message.startsWith("link")) {
            event.getChannel().sendMessage("https://discord.gg/tjskEJ7SFb").queue();
        }
        if (message.startsWith("ссылка")) {
            event.getChannel().sendMessage("https://discord.gg/tjskEJ7SFb").queue();
        }
    }
}
