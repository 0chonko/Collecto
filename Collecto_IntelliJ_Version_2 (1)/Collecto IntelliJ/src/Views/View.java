// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Views;

public interface View {
    /**
     * Displays a message in the view
     * @param message
     */
    public void displayMessage(String message);

    /**
     * Gets input from the view
     * @return a string picked up from the console
     */
    public String getStringInput();

    /**
     * Gets input from the view
     * @return an int picked up from the console
     */
    public int getIntInput();

    public void listCommands();
}
