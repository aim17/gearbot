package gearbot.commands;

import java.util.ArrayList;

import org.apache.commons.validator.routines.UrlValidator;

import java.sql.*;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import gearbot.Config;

public class GearCommand extends Command {
    
    private Connection connection;
    
    public GearCommand(Connection connection) {
        this.name = "gear";
        //this.arguments = "<item>...";
        this.connection = connection;
    }

    @Override
    protected void execute(CommandEvent event) {
        
        // Check to make sure the user did not try to directly upload an image.
        if (!event.getMessage().getAttachments().isEmpty()) {
            event.reply("Please upload the image to an image sharing website and use the link. (Attachments are not supported)");
        }
        
        // If the user did not supply any arguments, then display the user's gear.
        else if (event.getArgs().isEmpty()) {
            
            try {
                
                // Grab the results from the MySQL DB. Currently not very customizable without changing some code.
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery
                        ("SELECT id, picture FROM gear_data WHERE id = " + event.getAuthor().getIdLong());
                
                // If the resultset has no first result, then there is no entry for this user.
                if (rs.next() == false) {
                    event.reply("User's gear does not exist.");
                }
                
                // There is an entry for the user. Technically there isn't a need for a do-while loop since this
                // should always return one result.
                else {
                    do {
                        if (rs.getLong("id") == event.getAuthor().getIdLong()) {
                            event.reply(rs.getString("picture"));
                        }
                    } while(rs.next());
                }

            } catch (SQLException e) {
                event.reply("Database error occured.");
                e.printStackTrace();
            }
        }
        
        // If the user supplied arguments
        else {
            
            // Split the arguments into an array. User should only supply one argument.
            String[] items = event.getArgs().split("\\s+");
            
            // User correctly supplied only one argument.
            if (items.length == 1) {
                
                // Check if the argument given is a valid URL.
                String[] protocols = {"http", "https"};
                UrlValidator urlValidator = new UrlValidator(protocols);
                
                if(urlValidator.isValid(items[0])) {
                    
                    try {
                        
                        // This SQL query handles both a new user & updating an existing user.
                        Statement statement = connection.createStatement();
                        String sql = "INSERT INTO gear_data " + 
                                     "VALUES(" + event.getAuthor().getIdLong() + ", '" + items[0] + "')" +
                                     "ON DUPLICATE KEY UPDATE picture='" + items[0] + "'";
                        statement.executeUpdate(sql);
                        event.reply("Your gear has been added to the database.");
                        
                    } catch (SQLException e) {
                        event.reply("Database error occured.");
                        e.printStackTrace();
                    }
                }
                
                // This regex pattern checks if the user supplied an '@User' argument. (format: <@!#############> )
                else if(items[0].matches("(^<@!)([0-9]*)(>$)") && items.length == 1) {
                    
                    // Scrub the input string to obtain the user's ID for querying the DB.
                    // This nested .replaceAll() works, but probably not the best way to do it ÅP\_(Éc)_/ÅP
                    String scrubbed_id = items[0].replaceAll("(^<@!)", "").replaceAll("(>$)", "");
                    
                    try {
                        
                        // Same deal as before, except grab the results for the specified user in the '@User' argument.
                        Statement statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery("SELECT id, picture FROM gear_data WHERE id = " + Long.parseLong(scrubbed_id));
                        
                        if (rs.next() == false) {
                            event.reply("User's gear does not exist.");
                        }
                        else {
                            do {
                                if (rs.getLong("id") == Long.parseLong(scrubbed_id)) {
                                    event.reply(rs.getString("picture"));
                                }
                            } while(rs.next());
                        }
                    } catch (SQLException e) {
                        event.reply("Database error occured.");
                        e.printStackTrace();
                    }
                }
                
                // User did not provide the correct format.
                else {
                    event.reply("Incorrect format.");
                }
            }

            // User provided more than one argument.
            else {
                event.replyWarning("Please provide only one argument.");
            }
        }
    }
}
