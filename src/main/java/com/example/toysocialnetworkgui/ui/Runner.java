package com.example.toysocialnetworkgui.ui;

import com.example.toysocialnetworkgui.domain.Message;
import com.example.toysocialnetworkgui.domain.User;
import com.example.toysocialnetworkgui.domain.validators.ValidationException;
import com.example.toysocialnetworkgui.repository.repoExceptions.RepoException;
import com.example.toysocialnetworkgui.service.SuperService;

import java.util.*;

import static com.example.toysocialnetworkgui.Utils.constants.DomainConstants.ACTIVE_MESSAGE;
import static com.example.toysocialnetworkgui.Utils.constants.DomainConstants.SIMPLE_MESSAGE;
import static com.example.toysocialnetworkgui.Utils.constants.UiConstants.*;
import static com.example.toysocialnetworkgui.Utils.constants.RepoConstants.*;
import static com.example.toysocialnetworkgui.Utils.constants.ValidatorConstants.DATE_TIME_FORMATTER;


public class Runner {
    private SuperService superService = null;

    public Runner(SuperService superService){
        this.superService = superService;
    }

    private void mainShowAllFriendships(){
        System.out.println("De implementat");
    }

    private void mainShowAllUsers(){
        System.out.println("De implementat");
    }

    private void UIAddUser(){
        Scanner console = new Scanner(System.in);
        System.out.print("First name: ");
        String first_name = console.next();
        String valid_first_name = first_name.strip();
        System.out.print("Last name: ");
        String last_name = console.next();
        String valid_last_name = last_name.strip();
        int rez = superService.addUser(valid_first_name,valid_last_name);
        if(rez == SUCCESFUL_OPERATION_RETURN_CODE)
            System.out.println("User added succesfully!");
        else
            System.out.println("User already exists!");
    }

    private void  UIAddFriend(User user){
        Scanner console = new Scanner(System.in);
        System.out.println("First name: ");
        String name = console.next();
        String stripped_name = name.strip();
        List<User> usersMatchingName = superService.findUsersByName(stripped_name);
        if(usersMatchingName.size() == 0){
            System.out.println("No users found mathcing this name!");
            return;
        }
        for(int i=0;i<usersMatchingName.size();i++)
            System.out.println(String.format("%d. ", i) + usersMatchingName.get(i));
        try {
            System.out.println("Select user: ");
            System.out.print(">>> ");
            Integer selected_user_index = console.nextInt();
            if(selected_user_index<0 || selected_user_index >= usersMatchingName.size()){
                System.out.println("Invalid friend selected!");
                return;
            }
            int rez = superService.addFriendForGivenUser(user,usersMatchingName.get(selected_user_index));
            if(rez == 1)
                System.out.println("Friendship already exists!");
        } catch (InputMismatchException ex) {
            System.out.println("Invalid friend selected!");
        }
    }

    private void  UIDeleteFriend(User user){
        if(superService.getAllFriendsForGivenUser(user).size() == 0){
            System.out.println("This user has no friends! :(");
            return;
        }
        Scanner console = new Scanner(System.in);
        System.out.print("Friend name: ");
        String name = console.next();
        String stripped_name = name.strip();
        List<User> usersMatchingName = superService.findAllFriendsMathcingNameForGivenUser(user,stripped_name);
        if(usersMatchingName.size() == 0){
            System.out.println("No friends found mathcing this name!");
            return;
        }
        for(int i=0;i<usersMatchingName.size();i++)
            System.out.println(String.format("%d. ", i) + usersMatchingName.get(i));
        while(true) {
            try {
                System.out.println("Select the friend to be removed: ");
                System.out.print(">>> ");
                Integer selected_user_index = console.nextInt();
                if(selected_user_index<0 || selected_user_index >= usersMatchingName.size()){
                    System.out.println("Invalid friend selected!");
                    break;
                }
                superService.deleteFriendForUser(user,usersMatchingName.get(selected_user_index));
                return;

            } catch (InputMismatchException ex) {
                System.out.println("Invalid friend selected!");
                break;
            }
        }
    }

    private void  UIDeleteThisUser(User user){
       superService.removeUser(user);
    }

    private void  UIShowAllFriendsForThisUser(User user){
       Set<User> friends = superService.getAllFriendsForGivenUser(user);
       if(friends.size() == 0){
           System.out.println("No friends to show");
           return;
       }
       for(User friend: friends)
           System.out.println(friend);
    }

    private void showConversationBetweenTwoUsers(User user1, User user2){
        System.out.println("==================== CONVO ====================");
        List<Message> convo = superService.getMessagesBetweenTwoUsers(user1,user2);
        for(int i = 0; i < convo.size(); i++){
            String line = new String(new char[70]).replace('\0', ' ');
            Message current_message = convo.get(i);
            String body_message = null;
            if(current_message.getDeleteStatus().equals(ACTIVE_MESSAGE))
                body_message = current_message.getMesaj();
            else
                body_message = "<<deleted>>";
            User from = superService.findUserById(current_message.getIdFrom());
            if(convo.get(i).getIdReply()!=-1) {
                for (int j = 0; j < i; j++)
                    if (convo.get(j).getId().equals(convo.get(i).getIdReply())) {

                        String start_line = String.format("%d. --reply to %d-- %s: %s", i, j, from.getLastName(), body_message);
                        line = insertString(line, start_line, 0);
                        int date_length = current_message.getDataTrimitere().format(DATE_TIME_FORMATTER).length();
                        int index_to_insert_data = 85-date_length;
                        line = insertString(line,current_message.getDataTrimitere().format(DATE_TIME_FORMATTER),index_to_insert_data);
                        System.out.println(line);
                        break;
                    }
            }
            else {
                        String start_line = String.format("%d.                %s: %s", i, from.getLastName(), body_message);
                        line = insertString(line,start_line,0);
                        int date_length = current_message.getDataTrimitere().format(DATE_TIME_FORMATTER).length();
                        int index_to_insert_data = 85-date_length;
                        line = insertString(line,current_message.getDataTrimitere().format(DATE_TIME_FORMATTER),index_to_insert_data);
                        System.out.println(line);
            }
        }
    }

    private void UISendMessage(User user1, User user2){
        System.out.print("Insert new message: ");
        Scanner console = new Scanner(System.in);
        String message = console.nextLine();
        String stripped_message = message.strip();
        superService.addMessageBetweenTwoUsers(user1,user2,stripped_message,SIMPLE_MESSAGE);
    }

   private void UIReplyMessage(User user1,User user2){
       System.out.print("Message index to which you reply: ");
       Scanner console = new Scanner(System.in);
       String index = console.nextLine();
       int input_index = -1;
       try{
           input_index = Integer.parseInt(index);
       } catch (NumberFormatException ex){
           System.out.println("Invalid index!");
           return;
       }
       if(input_index < 0 || input_index >= superService.getMessagesBetweenTwoUsers(user1,user2).size()){
           System.out.println("Invalid index!");
           return;
       }
       System.out.print("New Reply: ");
       String message = console.nextLine();
       String stripped_message = message.strip();
       superService.addMessageBetweenTwoUsers(user1,user2,stripped_message,superService.getMessagesBetweenTwoUsers(user1,user2).get(input_index).getId());

   }

   private void UIDeleteMessage(User user1,User user2){
        List<Message> convo = superService.getMessagesBetweenTwoUsers(user1,user2);
        System.out.print("Index of message to be deleted: ");
        Scanner console = new Scanner(System.in);
        String index = console.nextLine();
        int input_index = -1;
        try{
            input_index = Integer.parseInt(index);
        } catch (NumberFormatException ex){
            System.out.println("Invalid index!");
            return;
        }
        if(input_index < 0 || input_index >=convo.size() || !convo.get(input_index).getIdFrom().equals(user1.getId())){
            System.out.println("Invalid index!");
            return;
        }
        superService.deleteMessage(convo.get(input_index));
    }

    private void UIUndoDeleteMessage(User user1,User user2){
        List<Message> convo = superService.getMessagesBetweenTwoUsers(user1,user2);
        System.out.print("Index of a deleted message: ");
        Scanner console = new Scanner(System.in);
        String index = console.nextLine();
        int input_index = -1;
        try{
            input_index = Integer.parseInt(index);
        } catch (NumberFormatException ex){
            System.out.println("Invalid index!");
            return;
        }
        if(input_index < 0 || input_index >=convo.size() || !convo.get(input_index).getIdFrom().equals(user1.getId()) || convo.get(input_index).getDeleteStatus().equals(ACTIVE_MESSAGE)){
            System.out.println("Invalid index!");
            return;
        }
        superService.undoDeleteMessage(convo.get(input_index));
    }
    private void runUserConversationMenu(User user1, User user2){
        while(true){
            showConversationBetweenTwoUsers(user1,user2);
            showConversationMenu();
            Scanner console = new Scanner(System.in);
            String command = console.next();
            String stripped_command = command.strip();
            try{
                switch (stripped_command){
                    case SEND_MESSAGE:
                        UISendMessage(user1, user2);
                        break;
                    case REPLY_TO_MESSAGE:
                        UIReplyMessage(user1,user2);
                        break;
                    case DELETE_MESSAGE:
                        UIDeleteMessage(user1,user2);
                        break;
                    case UNDO_DELETE_MESSAGE:
                        UIUndoDeleteMessage(user1,user2);
                        break;
                    case RETURN_TO_SELECTED_USER_OPERATIONS_MENU:
                        return;
                    default:
                        System.out.println("Invalid command!");
                }
            } catch (RepoException | ValidationException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private void UIShowConversation(User user){
        Scanner console = new Scanner(System.in);
        System.out.print("User name: ");
        String name = console.next();
        String stripped_name = name.strip();
        List<User> usersMatchingName = superService.findUsersByName(stripped_name);
        if(usersMatchingName.size() == 0){
            System.out.println("No users found mathcing this name!");
            return;
        }
        for(int i=0;i<usersMatchingName.size();i++)
            System.out.println(String.format("%d. ", i) + usersMatchingName.get(i));
        while(true) {
            try {
                System.out.println("Select a user: ");
                System.out.print(">>> ");
                Integer selected_user_index = console.nextInt();
                if(selected_user_index<0 || selected_user_index >= usersMatchingName.size()){
                    System.out.println("Invalid user selected!");
                    break;
                }
                runUserConversationMenu(user,usersMatchingName.get(selected_user_index));
                return;

            } catch (InputMismatchException ex) {
                System.out.println("Invalid user selected!");
                break;
            }
        }
    }

    private void  runSelectedUserMenu(User user){
        while(true){
            showSelectedUserMenu();
            Scanner console = new Scanner(System.in);
            String command = console.next();
            String stripped_command = command.strip();
            try{
                switch (stripped_command){
                    case ADD_FRIEND:
                        UIAddFriend(user);
                        break;
                    case DELETE_FRIEND:
                        UIDeleteFriend(user);
                        break;
                    case DELETE_THIS_USER:
                        UIDeleteThisUser(user);
                        return;
                    case SHOW_ALL_FRIENDS_FOR_THIS_USER:
                        UIShowAllFriendsForThisUser(user);
                        break;
                    case SHOW_CONVERSATION_BETWEEN_TWO_USERS:
                        UIShowConversation(user);
                        break;
                    case RETURN_TO_USER_OPERATIONS:
                        return;
                    default:
                        System.out.println("Invalid command!");
                }
            }
            catch (RepoException | ValidationException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private void UISelectUser(){
        Scanner console = new Scanner(System.in);
        System.out.print("User name: ");
        String name = console.next();
        String stripped_name = name.strip();
        List<User> usersMatchingName = superService.findUsersByName(stripped_name);
        if(usersMatchingName.size() == 0){
            System.out.println("No users found mathcing this name!");
            return;
        }
        for(int i=0;i<usersMatchingName.size();i++)
            System.out.println(String.format("%d. ", i) + usersMatchingName.get(i));
       while(true) {
           try {
               System.out.println("Select a user: ");
               System.out.print(">>> ");
               Integer selected_user_index = console.nextInt();
               if(selected_user_index<0 || selected_user_index >= usersMatchingName.size()){
                   System.out.println("Invalid user selected!");
                   break;
               }
               runSelectedUserMenu(usersMatchingName.get(selected_user_index));
               return;

           } catch (InputMismatchException ex) {
               System.out.println("Invalid user selected!");
               break;
           }
       }
    }

    private void runUserOperationMenu(){
        while(true){
            showUserOperationsMenu();
            Scanner console = new Scanner(System.in);
            String command = console.next();
            String stripped_command = command.strip();
            try{
                switch (stripped_command){
                    case ADD_USER:
                        UIAddUser();
                        break;
                    case SELECT_USER:
                        UISelectUser();
                        break;
                    case EXIT_USER_OPERATIONS:
                        return;
                    default:
                        System.out.println("Invalid command!");
                }
            }
            catch (RepoException | ValidationException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private void runMainMenu(){
        while(true){
            showMainMenu();
            Scanner console = new Scanner(System.in);
            String command = console.next();
            String stripped_command = command.strip();
            try{
                switch (stripped_command){
                    case MAIN_SHOW_ALL_USERS:
                        mainShowAllUsers();
                        break;
                    case MAIN_SHOW_ALL_FRIENDSHIPS:
                        mainShowAllFriendships();
                        break;
                    case MAIN_USER_OPERATIONS:
                        runUserOperationMenu();
                        break;
                    case MAIN_EXIT_APP:
                        System.out.println("Sayonara");
                        return;
                    default:
                        System.out.println("Invalid command!");
                }
            }
            catch (RepoException | ValidationException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    public void runApp(){
        runMainMenu();
    }

    private void showMainMenu(){
        System.out.println("==================== MENU ====================");
        System.out.println("1. Show all users");
        System.out.println("2. Show all friendships");
        System.out.println("3. graf");
        System.out.println("4. graf");
        System.out.println("5. User Operations");
        System.out.println("6. Exit app");
        System.out.print(">>> ");
    }

    private void showUserOperationsMenu(){
        System.out.println("==================== USER OPERATIONS MENU ====================");
        System.out.println("1. Add new User");
        System.out.println("2. Select User");
        System.out.println("3. Return to main Menu");
        System.out.print(">>> ");
    }

    private void showSelectedUserMenu(){
        System.out.println("==================== SELECTED USER MENU ====================");
        System.out.println("1. Add friend");
        System.out.println("2. Delete friend");
        System.out.println("3. Delete this user");
        System.out.println("4. Show all friendships for this user");
        System.out.println("5. Show conversation between this user and another");
        System.out.println("6. Return to User Operations Menu");
        System.out.print(">>> ");
    }

    private void showConversationMenu(){
        System.out.println("==================== CONVERSATION MENU ====================");
        System.out.println("1. Send new message");
        System.out.println("2. Reply to a certain message");
        System.out.println("3. Delete message");
        System.out.println("4. Undo delete message");
        System.out.println("5. Return to Selected User Operations Menu");
        System.out.print(">>> ");
    }

    private String insertString(String originalString, String stringToBeInserted, int index) {
        String newString = new String();
        for (int i = 0; i < originalString.length(); i++) {
            newString += originalString.charAt(i);
            if (i == index) {
                newString += stringToBeInserted;
            }
        }
        return newString;
    }

}
