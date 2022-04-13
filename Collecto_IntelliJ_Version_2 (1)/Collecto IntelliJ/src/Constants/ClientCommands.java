// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Constants;

public class ClientCommands {
    public static final String HELP = "help";
    public static final String LIST = "list";
    public static final String HINT = "hint";

    public static boolean contains(String command) {
        return command.equalsIgnoreCase(HELP) || command.equalsIgnoreCase(LIST)
                || command.equalsIgnoreCase(HINT);
    }
}
