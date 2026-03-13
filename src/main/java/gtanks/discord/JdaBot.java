package gtanks.discord;

import gtanks.discord.commands.Moderator.ClearChatCommand;
import gtanks.discord.commands.Default.DiscordLinkCommand;
import gtanks.discord.commands.Owner.HelpCommand;
import gtanks.discord.commands.Default.OnlineCommand;
import gtanks.discord.commands.Default.PingCommand;
import gtanks.discord.commands.Owner.RestartCommand;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class JdaBot {

    private static final Logger logger = LoggerFactory.getLogger(JdaBot.class);

    public static void initialize(String token) throws InterruptedException, LoginException {
        JDABuilder builder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new BotEventListener())
                .addEventListeners(new PingCommand())
                .addEventListeners(new DiscordLinkCommand())
                .addEventListeners(new HelpCommand())
                .addEventListeners(new OnlineCommand())
                .addEventListeners(new RestartCommand())
                .addEventListeners(new ClearChatCommand())
                .setActivity(Activity.playing("FlashTanki"));

        builder.build().awaitReady();
        logger.info("Bot is ready.");
    }

    private static class BotEventListener extends ListenerAdapter {
        @Override
        public void onReady(ReadyEvent event) {
            logger.info("Bot started!");
        }
    }
}
