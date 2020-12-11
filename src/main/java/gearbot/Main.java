package gearbot;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import gearbot.commands.GearCommand;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
        
        // Initialize a connection to the MySQL DB. This will get passed to any command classes that need it.
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" + 
                 Config.getSetting("db.url") + ":" + 
                 Config.getSetting("db.port") + "/" + 
                 Config.getSetting("db.name"),
                 Config.getSetting("db.user"), 
                 Config.getSetting("db.password"));
        
        EventWaiter waiter = new EventWaiter();
        
        CommandClientBuilder client = new CommandClientBuilder()
                .setPrefix("-")
                .setStatus(OnlineStatus.ONLINE)
                .setOwnerId(Config.getSetting("owner.id"))
                .setActivity(Activity.watching("Pokimane"))
                
                // Command initialization.
                .addCommand(new GearCommand(connection));
        
        JDA api = JDABuilder
                .createDefault(Config.getSetting("bot.token"))
                .addEventListeners(waiter, client.build())
                .build();

    }

}
