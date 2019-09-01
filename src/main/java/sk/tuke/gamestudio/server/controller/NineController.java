package sk.tuke.gamestudio.server.controller;
//vytvorit cisto game bez moznosti multipl, refactor kodu, presunut logiku do samostatnej tredy, grafika, websocket

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.Game;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.GameState;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.Tile;
import sk.tuke.gamestudio.game.hubacek.ninemen.gameCore.TileState;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.ScoreService;

import javax.servlet.ServletContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/mlyny")
public class NineController {

    /**
     * games - all games online
     * multiplayer - all multiplayer games (2 controllers)
     * disable - disable/enable second controller
     */
    private static Map<Integer, Game> games = new HashMap<>();
    private static Map<Integer, Boolean> multiplayer = new HashMap<>();
    private static Map<Integer, Integer> disable = new HashMap<>();


    /**
     * wipe - at midnight
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private ServletContext servletContext;

    private Game game;

    private boolean controller = false;

    private int displayId;

    //if 0, first controller, if >0 second controller (for multiplayer)
    private int connectId = 0;

    //0 - false, 1 - true, 2 - connected
    private int disableValue = 0;


    /**
     * set wipe time
     */
    public NineController() {
        Long midnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::clear, midnight, 1, TimeUnit.DAYS);
    }

    /**
     * wipe function
     */
    private void clear() {
        games.clear();
        multiplayer.clear();
        disable.clear();
    }

    /**
     * main function, process input, when field is changed, return to /mlyny through redirecting page (to clear url parameters)
     *
     * @param rw    - row
     * @param col   - column
     * @param model - something mysterious
     * @return
     */
    @RequestMapping
    public String mlyny(String rw, String col, Model model) {
//        updateModel(model);
        controller = true;
        if (process(rw, col, model) == "mlynyRedirect") {
            return "mlynyRedirect";
        }
        return "mlyny";
    }


    /**
     * logic of game - probably will move to other class (and refactor, its ugly)
     * on winning automatically add winner to score table in curr DB
     * @param rw - row
     * @param col - column
     * @param model - still a huge mystery
     * @return
     */
    @RequestMapping("/game")
    public String process(String rw, String col, Model model) {
        if (displayId > 0) {
            game = games.get(displayId);
        }
        if (game == null) {
            newGame();
        }
        int row = 10;
        int column = 10;
        try {
            row = Integer.parseInt(rw);
            column = Integer.parseInt(col);
        } catch (Exception e) {
//            e.printStackTrace();
            updateModel(model);
            return "mlyny";
        }
        if (!(getClickable(game.getField().getTile(row, column), row, column))) {
            updateModel(model);
            return "mlyny";
        }

        while ((game.gameState() != GameState.P1WIN) || (game.gameState() != GameState.P2WIN)) {
            while (game.gameState() != GameState.REPLACING) {
                if ((game.gameState().equals(GameState.P1WIN)) || (game.gameState().equals(GameState.P2WIN))) {
                    break;
                }
                if ((row < 7) && (column < 7)) {
                    if (!game.getKilling()) {
                        if (game.move(row, column)) {
                            if (game.searchForRow(row, column)) {
                                game.setKilling(true);
                            } else {
                                game.setKilling(false);
                            }
                            updateModel(model);
                            return "mlynyRedirect";
                        }
                    } else {
                        selectWhomToKill(row, column);
                    }
                    updateModel(model);
                    return "mlynyRedirect";
                }
            }
            replacing(row, column);
            updateModel(model);
            if ((game.gameState().equals(GameState.P1WIN)) || (game.gameState().equals(GameState.P2WIN))) {
                break;
            }
            updateModel(model);
            return "mlynyRedirect";
        }
        if (game.gameState().equals(GameState.P1WIN)) {
            game.setActive(game.getPlayer1());
            scoreService.addScore(new Score("Mlyny", game.getActive().getName(), game.getActive().getFinalScore(), new Date()));
        } else if (game.gameState().equals(GameState.P2WIN)) {
            game.setActive(game.getPlayer2());
            scoreService.addScore(new Score("Mlyny", game.getActive().getName(), game.getActive().getFinalScore(), new Date()));

        }
        updateModel(model);
        return "mlynyRedirect";
    }


    /**
     * another great game logic function, def. move to same class as ^^
     * @param row
     * @param col
     */
    private void replacing(int row, int col) {
        if (!game.getSelecting() && !game.getKilling()) {
            if (game.selectYourTile(row, col)) {
                game.setSelecting(true);
                game.setTmpx(row);
                game.setTmpy(col);
                game.getField().getTile(row, col).setState(TileState.Empty);
            }
        } else if (game.getKilling()) {
            selectWhomToKill(row, col);
        } else if (game.getSelecting()) {
            game.setSelecting(false);
            game.move(row, col);
            if (game.searchForRow(row, col)) {
                game.setKilling(true);
            } else {
                game.setKilling(false);
            }
        }
    }


    /**
     * will move same as ^^
     * @param row
     * @param col
     */
    private void selectWhomToKill(int row, int col) {
        if (game.searchAndDestroy(row, col)) {
            game.setKilling(false);
        }
    }

    /**
     * @return game field || empty string in case of game == null
     */
    public String getHtmlField() {
        if (game == null) {
            return "";
        }
        return getHtmlField(game);
    }

    /**
     * creates html table from game
     * @param game
     * @return game in html
     */
    private String getHtmlField(Game game) {

        StringBuilder sb = new StringBuilder();
        sb.append("<p id = 'reload'>");
        sb.append("<div class = 'stat'>Hrac: "
                + game.getActive().getPlayerNumber()
                + ", score : "
                + game.getActive().getScore()
                + ", farba: "
                + String.format("<img src='%s/images/%s.jpg' class='game'>", servletContext.getContextPath(), getImageName(game.getActive().getTileState())) + "</div>");

//        sb.append("<a href='" +
//                String.format("%s/mlyny?rw=%s&col=%s&command=%s", servletContext.getContextPath(), 1, 1, 1)
//                + "'>\n");
//        sb.append("NEW GAME");
        sb.append("</a>");
//bgcolor='black'
        sb.append("<table>");
        for (int row = 0; row < 7; row++) {
            sb.append("<tr>\n");
            for (int column = 0; column < 7; column++) {
                Tile tile = game.getField().getTile(row, column);
                sb.append("<td>\n");
                if (getClickable(tile, row, column)) {
//                    if (game.equals(this.game))
                    sb.append("<a href='" +
                            String.format("%s/mlyny?rw=%s&col=%s", servletContext.getContextPath(), row, column)
                            + "'>\n");
                }
                sb.append(String.format("<img src='%s/images/%s.jpg' class='game'>", servletContext.getContextPath(), getImageName(tile.getState())));
//                sb.append("<img src='/images/" + getImageName(tile.getState()) + ".jpg' style='width:32px;height:32px;'>");
//                if (game.equals(this.game))
                sb.append("</a>");
                sb.append("</td>\n");

            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        if (game.gameState() == GameState.P1WIN || game.gameState() == GameState.P2WIN) {
            sb.append("<div class = 'center stat' >Hrac: " + game.getActive().getName() + " WON!!!</div>");
        }
        sb.append("</p>");
        return sb.toString();
    }


    /**
     * choose which tile will be clickable at the exact time, depending on player and game state
     * @return
     */
    private boolean getClickable(Tile tile, int row, int col) {
        if (multiplayer != null) {
            if (multiplayer.containsKey(displayId)) {
                if (connectId > 0) {
                    if (game.getActive().getPlayerNumber() != 2) {
                        return false;
                    }
                } else if (game.getActive().getPlayerNumber() != 1) {
                    return false;
                }
            }
        }

        if (game.getKilling()) {
            return tile.getState() == game.getEnemy().getTileState() && (game.checkIfAttackPossible(row, col));
        } else if (game.gameState() == GameState.REPLACING) {
            if (!game.getSelecting()) {
                if (tile.getState() == game.getActive().getTileState()) {
                    //dorobit pre iba tie tile, kt sa mozu hybat
                    return game.checkIfSelectPossible(row, col);
                }
            } else {
                return game.checkReplacementPossible(game.getTmpx(), game.getTmpy(), row, col);
            }
        } else return tile.getState() == TileState.Empty;
        return false;
    }


    /**
     * create String, that represents image file, from curr. tile
     * @param tileState
     * @return name of img without .*
     */
    private String getImageName(TileState tileState) {
        switch (tileState) {
            case Next:
                return "Next";
            case Empty:
                return "Empty";
            case P1:
                return "shP1";
            case P2:
                return "shP2";
            case Stop:
                return "Stop";
            case Vertical:
                return "Vertical";
            case Horizontal:
                return "Horizontal";
        }
        return "Stop";
    }

    /**
     * magically creates a variable, which will be accessible in html, using thymeleaf
     * @param model
     */
    private void updateModel(Model model) {
        model.addAttribute("displayId", displayId);
        if (!controller) {
            model.addAttribute("scores", getScores());
            model.addAttribute("avgRating", getAvgRating());

        }
        if (game != null) {
            if (game.getPlayer1().getName() != null) {
                model.addAttribute("player1", game.getPlayer1().getName());
            }
            if (game.getPlayer2().getName() != null) {
                model.addAttribute("player2", game.getPlayer2().getName());
            }
            if (connectId != 0) {
                model.addAttribute("connect", connectId);
            }
            if (disable.get(displayId) != null) {
                disableValue = disable.get(displayId);
            }
            model.addAttribute("disable", disableValue);

        }
    }

    public String getScores() {
        StringBuilder sb = new StringBuilder();
        int a = 0;
        for (Score s : scoreService.getBestScores("Mlyny")) {
            sb.append("<div class = 'line'><div class = 'pl'>Player: " + s.getPlayer() + "</div>" + "<div class = 'sc'>" + "Score: " + s.getPoints() + "</div></div><br>");
            a++;
            if (a == 5) {
                break;
            }
        }
        return sb.toString();
    }

    public String getComments() {
        StringBuilder sb = new StringBuilder();
        int a = 0;
        for (Comment c : commentService.getComments("Mlyny")) {
            sb.append("<div class = 'line'><div class = 'plc'>Player: " + c.getPlayer() + "</div>" + "<br><div class = 'cc'>" + " Comment: " + c.getComment() + "</div></div><br>");
            a++;
            if (a == 15) {
                break;
            }
        }
        return sb.toString();
    }

    public String getAvgRating() {
        StringBuilder sb = new StringBuilder();
        int a = ratingService.getAverageRating("Mlyny");
        float b = a / 10;
        sb.append("<div class = 'avgRating'>Average Rating: " + b + "</div>");
        return sb.toString();
    }

    /**
     * creates new game, send new game to static Map, for multiplayer sync
     * @param model
     * @return
     */
    @RequestMapping("/new")
    public String newGame(Model model) {
        newGame();
        if (displayId != 0) {
            games.replace(displayId, game);
        }
        updateModel(model);
        return "mlyny";
    }


    private void newGame() {
        game = new Game();
//        if (displayId != 0)
//            games.put(displayId, game);

    }

    /**
     * pair first controller, or secondary controller for multiplayer purpose
     * ugly(will rewrite in future)
     * @param id
     * @param model
     * @return template mlyny in case of success || redirect home if game already has 2 active controllers
     */
    @RequestMapping("/display/pair/{id}")
    public String pair(@PathVariable int id, Model model) {
        //second player
        if (games.get(id) != null) {
            if (disable.get(id) == 1) {
                return "redirectHome";
            }
            if (displayId > 0) {
                updateModel(model);
                return "mlyny";
            }
            displayId = id;
            connectId = id;
            controller = true;
            game = games.get(id);
            disableValue = 3;
            disable.put(id, 3);
            multiplayer.put(id, true);
        }
        //try for fun, probably can delete now, but i wont touch it
        try {
            controller = true;
            displayId = id;
            if (game == null) {
                newGame();
            }
            if (!games.containsKey(id)) {
                games.put(id, game);
            }
            if (!disable.containsKey(id)) {
                disable.put(id, 0);
            }
            updateModel(model);
        } catch (Exception e) {
            e.getMessage();
        }
        return "mlyny";
    }

    /**
     * unpair and redirect home
     * @param model
     * @return
     */
    @RequestMapping("/display/unpair")
    public String pair(Model model) {
        games.remove(displayId);
        multiplayer.remove(displayId);
        disable.remove(displayId);
        game = null;
        displayId = 0;
        updateModel(model);
        return "redirectHome";
    }

    /**
     * display game on 3rd screen, (not designed to be controller)
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/display/{id}")
    public String display(@PathVariable int id, Model model) {
        displayId = id;
        game = games.get(id);
        if (game != null) {
            model.addAttribute("htmlField", getHtmlField(game));
        }
        updateModel(model);
        return "mlyny_view";
    }

    /**
     * find an empty session in static Map games
     * @param model
     * @return
     */
    @RequestMapping("/display/session")
    public String findSession(Model model) {
        int i = 0;
        do {
            i++;
            game = games.get((i));
        } while (game != null);
        displayId = i;
        updateModel(model);
        return "redirect";
    }

    /**
     * login player 1 || player 2
     * @param login
     * @param model
     * @return
     */
    @RequestMapping("/login")
    public String login(String login, Model model) {
        if (game != null) {
            if (game.getPlayer1().getName() == null) {
                game.getPlayer1().setName(login);
            } else {
                game.getPlayer2().setName(login);
            }
        }
        updateModel(model);
        return "mlyny";
    }

    @RequestMapping("/comment")
    public String comment(String comment, Model model) {
        String name;
        if (connectId > 0) {
            name = game.getPlayer2().getName();
        } else {
            name = game.getPlayer1().getName();
        }
        commentService.addComment(new Comment(name, "Mlyny", comment, new Date()));
        updateModel(model);
        return "mlynyRedirect";
    }


    @RequestMapping("/rating")
    public String rating(String rating, Model model) {
        updateModel(model);
        return "mlyny";
    }


    /**
     * logout player 1 || player 2
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/logout/{id}")
    public String logout(@PathVariable int id, Model model) {
        if (id == 1) {
            game.getPlayer1().setName(null);
        } else {
            game.getPlayer2().setName(null);
        }
        updateModel(model);
        return "mlyny";
    }

    /**
     * disable multicontroller multiplayer
     * @param model
     * @return
     */
    @RequestMapping("/disable")
    public String disable(Model model) {
        if (displayId > 0) {
            disable.replace(displayId, 1);
        }
        disableValue = 1;
        updateModel(model);
        return "mlyny";
    }

    /**
     * enable multicontroller multiplayer
     * @param model
     * @return
     */
    @RequestMapping("/enable")
    public String enable(Model model) {
        try{
        if (disable.get(displayId) == 3) {
            updateModel(model);
            return "mlyny";
        }}
        catch (Exception e){
            System.out.println("Chyba: " + e.getMessage());
        }
        if (displayId > 0) {
            disable.replace(displayId, 0);
        }
        disableValue = 0;
        updateModel(model);
        return "mlyny";
    }

    @RequestMapping("/welcomePage")
    public String redirectToWelcomePage() {
        return "welcomePage";
    }
}