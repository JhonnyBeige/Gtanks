package gtanks.discord.commands.Default;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PingCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().toLowerCase();

        if (message.startsWith("en?ping")) {
            event.getChannel().sendMessage("Pong! `@here`").queue();
        }
        if (message.startsWith("ru?ping")) {
            event.getChannel().sendMessage("Понг! `@here`").queue();
        }
    }
}
