package sk.tuke.gamestudio.game.hubacek.ninemen.consoleUI;

import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.Game;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.GameState;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.ScoreService;
import sk.tuke.gamestudio.service.exception.CommentException;
import sk.tuke.gamestudio.service.exception.RatingException;

import java.util.List;
import java.util.Scanner;

public class Console {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_WHITE = "\u001B[37m";

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;

    private final Game game = new Game();

    public Console() {}

    public static void main(String[] args) {
        Console console = new Console();
        console.start();
    }

    public void start(){
            System.out.println("-----------------Welcome!-----------------");
            System.out.println("Player 1 name: ");
            String name1 = new Scanner(System.in).nextLine().trim().toUpperCase();
            game.getActive().setName(name1);
            System.out.println("Player 2 name: ");
            String name2 = new Scanner(System.in).nextLine().trim().toUpperCase();
        game.getEnemy().setName(name2);
            update();
            while ((game.gameState() != GameState.P1WIN) || (game.gameState() != GameState.P2WIN)) {
                while (game.gameState() != GameState.REPLACING) {
                    if((game.gameState().equals(GameState.P1WIN)) || (game.gameState().equals(GameState.P2WIN))){
                        break;
                    }
                    int[] input = read();
                    int row = input[0];
                    int col = input[1];
                    if ((row < 7) && (col < 7)) {
                        if (!(game.move(row, col))) {
                            System.out.println(ANSI_RED + "------------------Tile is not Empty !!!------------------" + ANSI_RESET);
                            System.out.println(ANSI_RED + "---------------Please selectYourTile empty Tile !!!--------------" + ANSI_RESET);
                        }
                        if(game.searchForRow(row, col)){
                            selectWhomToKill();
                        }
                        update();
                    }
                }
                if((game.gameState().equals(GameState.P1WIN)) || (game.gameState().equals(GameState.P2WIN))){
                    break;
                }
                replacing();

            }
            if (game.gameState().equals(GameState.P1WIN)){
                System.out.println(ANSI_RED + "----------------------P1WON!!!!!!!!----------------------" + ANSI_RESET);
                game.setActive(game.getPlayer1());
            }
            else if(game.gameState().equals(GameState.P2WIN)){
                System.out.println(ANSI_RED + "----------------------P2WON!!!!!!!!----------------------" + ANSI_RESET);
                game.setActive(game.getPlayer2());
            }

        scoreService.addScore(new Score("Mlyny", game.getActive().getName(), game.getActive().getScore(), new java.util.Date()));


        for (int i = 0; i < 100; i++) {
                services();
                System.out.println();
            }

        System.exit(0);
    }


    private void services() {
        System.out.println("-----Do you want to rate or comment this ninemen ?-----");
        System.out.println("[1] - player " + game.getPlayer1().getName() + ", [2] - player " + game.getPlayer2().getName() + ", [X] - exit");
        System.out.println("[A] - view average rating, [T] - view top scores, [C] - view comments");
        String who = new Scanner(System.in).nextLine().trim().toUpperCase();
        switch (who) {
            case "1":
                game.setActive(game.getPlayer1());
                break;
            case "2":
                game.setActive(game.getPlayer2());
                break;
            case "X":
                System.exit(0);
            case "A": {
                try {
                    System.out.println("Avg rating je: " + ratingService.getAverageRating("Mlyny"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return;
            }
            case "T": {
                try {
                    List<Score> scoreList = scoreService.getBestScores("Mlyny");
                    for (Score s : scoreList) {
                        System.out.println("Name: " + s.getPlayer() + ", score: " + s.getPoints());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return;
            }
            case "C": {
                try {
                    List<Comment> commentList = commentService.getComments("Mlyny");
                    for (Comment c : commentList) {
                        System.out.println("Name: " + c.getPlayer() + ", Comment: " + c.getComment());
                    }
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            default:
                services();
                return;
        }

        System.out.println("[R] - rate this ninemen, [C] - add comment, [X] - exit");
        String input = new Scanner(System.in).nextLine().trim().toUpperCase();
        if("R".equals(input)){
            System.out.println("-----Please rate from 0 to 100-----");
            int rating = new Scanner(System.in).nextInt();
            try {
                ratingService.setRating(new Rating(game.getActive().getName(), "Mlyny", rating, new java.util.Date()));
            } catch (RatingException e) {
                e.printStackTrace();
            }
            System.out.println("--------Rating added successfully.--------");
        }
        else if("C".equals(input.toUpperCase())){
            System.out.println("-----Please write your comment below-----");
            String comment = new Scanner(System.in).nextLine().trim();
            try {
                commentService.addComment(new Comment(game.getActive().getName(), "Mlyny", comment, new java.util.Date()));
            } catch (CommentException e) {
                e.printStackTrace();
            }
            System.out.println("--------Comment added successfully.--------");
        }
        else if("X".equals(input.toUpperCase())){
            System.exit(0);
        }
        else{
            services();
        }

    }

    private void replacing() {
        System.out.println(ANSI_RED + "-------------Your are now in the replacing mode !!!-----------" + ANSI_RESET);
        System.out.println(ANSI_RED + "---------------Please selectYourTile your Tile to move--------------" + ANSI_RESET);

        int[] input = read();
        int row = input[0];
        int col = input[1];

        if ((row < 7) && (col < 7)) {
            if (game.selectYourTile(row, col)) {
                System.out.println(ANSI_RED + "-----This Tile ? (press U to undo, write location for moving)------" + ANSI_RESET);
                update();
            } else {
                System.out.println(ANSI_RED + "Wrong tile, choose your own tile!!!" + ANSI_RESET);
                update();
                return;
            }

            int[] inpt = read();
            int rw = inpt[0];
            int cl = inpt[1];
            if (inpt[2] != 0) {
                game.getField().getTile(row, col).setState(game.getActive().getTileState());
                update();
                return;
            }

            if((rw == row) && (cl == col)){
                game.getField().getTile(row, col).setState(game.getActive().getTileState());
                System.out.println(ANSI_RED + "You cant place it on the same spot!!!" + ANSI_RESET);
                update();
                return;
            }

            if(!(game.replacementPossible(row, col, rw, cl))){
                System.out.println(ANSI_RED + "Wrong Destination!!!" + ANSI_RESET);
                update();
                return;
            } else {
                game.move(rw, cl);
                if (game.searchForRow(rw, cl)) {
                    selectWhomToKill();
                }
            }

            update();
        }
    }

    private void selectWhomToKill() {

        System.out.println(ANSI_RED + "-------------Your are now in the killing mode !!!-----------" + ANSI_RESET);
        System.out.println(ANSI_RED + "Please selectYourTile which opponents tile, that is not part of a row you want to destroy" + ANSI_RESET);

        update();

        int[] input = read();
        int row = input[0];
        int col = input[1];

        if(!(game.searchAndDestroy(row,col))){
            System.out.println(ANSI_RED + "-------Wrong tile, choose an opponent tile that is not part of a row of 3!!!------" + ANSI_RESET);
            update();
            selectWhomToKill();
        }
    }

    private int[] read() {
        int[] result = new int[3];

        String inpt = new Scanner(System.in).nextLine().trim().toUpperCase();
        if ("X".equals(inpt)) {
            System.exit(0);
        }
        if ("U".equals(inpt)) {
            result[2] = 1;
        }
        if("".equals(inpt)){
            System.out.println(ANSI_RED + "--------------Wrong Input!!!--------------" + ANSI_RESET);
            System.out.println(ANSI_RED + "--------------Try Again!!!--------------" + ANSI_RESET);
            update();
            return read();
        }

        try {
            result[0] = inpt.charAt(0) - 'A';
            result[1] = inpt.charAt(1) - '1';
            for (int i = 0; i < 2; i++) {
                if ((result[i] > 6) || (result[i] < 0)) {
                    System.out.println(ANSI_RED + "--------------Wrong Input!!!--------------" + ANSI_RESET);
                    System.out.println(ANSI_RED + "--------------Try Again!!!--------------" + ANSI_RESET);
                    update();
                    return read();
                }
            }
        }
        catch (StringIndexOutOfBoundsException e){
            //pohodicka
        }
        return result;
    }

    private void update() {
        game.checkForWin();
        int a = 64;
        System.out.println("-------------------------GAME-------------------------");
        System.out.println();
        System.out.print("Player: " + game.getActive().getName() + "\tPlayer color: ");
        if(game.getActive().getPlayerNumber() == 1){
            System.out.print(ANSI_BLUE + "⚽" + ANSI_RESET);
        }
        else {
            System.out.print(ANSI_RED + "⚽" + ANSI_RESET);
        }
        if (!game.gameState().equals(GameState.REPLACING)) {
            System.out.println("\t\tRocks remaining: " + game.getActive().getRocks());
        } else {
            System.out.println("\t\tPlayer score: " + game.getActive().getScore());
        }
        System.out.println();
        System.out.println("    1       2       3       4       5       6       7");

        for (int x = 0; x<7; x++){
            a++;
            char b = (char) a;
            System.out.print(b + " ");
            for (int y = 0; y<7; y++){
                switch (game.getField().getTile(x, y).getState()) {
                    case Empty:
                        System.out.print(ANSI_WHITE + "\t⚫" + ANSI_RESET + "\t");
                        break;
                    case P1:
                        System.out.print(ANSI_BLUE + "\t⚽" + ANSI_RESET + "\t");
                        break;
                    case P2:
                        System.out.print(ANSI_RED + "\t⚽" + ANSI_RESET + "\t");
                        break;
                    default:
                        System.out.print("\t\t");
                        break;
                }
            }
            System.out.println();
            System.out.println();
        }
        System.out.println();
        System.out.println("-------------------------GAME-------------------------");
    }
}