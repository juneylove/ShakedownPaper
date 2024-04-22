package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Index;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextFormat {

    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, TextColor> CHAT_COLORS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, NamedTextColor> NAMED_COLORS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, org.bukkit.Color> UI_COLORS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, Integer> INT_COLORS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, String> TEAM_LOGOS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, Material> GLASS_COLORS = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<String, Material> CONCRETE_COLORS = new HashMap<>();

    public static final TextComponent TEAM_MEMBER = Component.text("\uE040");
    public static final TextComponent TEAM_MEMBER_OUTLINE = Component.text("\uE041", NamedTextColor.WHITE);
    public static final TextComponent DEAD_MEMBER = TEAM_MEMBER.color(NamedTextColor.DARK_GRAY).append(TextFormat.negativeSpace(9)).append(TEAM_MEMBER_OUTLINE);
    public static final TextComponent DEAD_TEAM = Component.text("\uE020");

    private static final TextColor DEFAULT_CHAT_COLOR = TextColor.color(0xAAAAAA);
    private static final NamedTextColor DEFAULT_NAMED_COLOR = NamedTextColor.GRAY;
    private static final org.bukkit.Color DEFAULT_UI_COLOR = org.bukkit.Color.fromRGB(170, 170, 170);
    private static final Integer DEFAULT_INT_COLOR = 170<<16 | 170<<8 | 170;

    public static final TextColor SCORE_COLOR = TextColor.color(0xffc338);
    public static final TextColor HIGHLIGHT_COLOR = TextColor.color(0xfadf91);
    public static final TextColor TITLE_COLOR = TextColor.color(0xa480ff);
    public static final TextColor ACCENT_COLOR = TextColor.color(0xcebaff);

    private static final TextColor NO_SHADOW = TextColor.color(0xffff00);

    public static final String PLUS_ONE_PX = "\uE000";
    public static final String NEGATIVE_ONE_PX = "\uE001";
    public static final String NEGATIVE_SIX_PX = "\uE002";
    public static final String THREE_PX_BACKGROUND = "\uE003" + NEGATIVE_ONE_PX;
    public static final String EIGHT_PX_BACKGROUND = "\uE004" + NEGATIVE_ONE_PX;
    public static final String BLANK_1X3 = "\uE005";
    public static final String BLANK_1X1 = "\uE006";
    public static final String RAISED_AT = "\uE00C";

    private static final char px1   = '\uE000';
    private static final char neg1  = '\uE001';
    private static final char neg6  = '\uE002';
    private static final char bg3px = '\uE003';
    private static final char bg8px = '\uE004';

    //=========================================================================

    public static void LoadTeamData(YamlConfiguration data) {

        for (String team : data.getKeys(false)) {

            int r = data.getInt(team + ".color.red");
            int g = data.getInt(team + ".color.green");
            int b = data.getInt(team + ".color.blue");

            CHAT_COLORS.put(team, TextColor.color(r, g, b));
            UI_COLORS.put(team, org.bukkit.Color.fromRGB(r, g, b));
            INT_COLORS.put(team, (r<<16 | g<<8 | b));

            TEAM_LOGOS.put(team, data.getString(team + ".logo"));

            String namedColor = (data.getString(team + ".named_color"));
            if (namedColor != null) {
                NAMED_COLORS.put(team, NamedTextColor.NAMES.value(namedColor.replace(' ', '_')));
            }

            String blockColor = data.getString(team + ".block_color");
            Material glass = Material.valueOf((blockColor + " stained glass").toUpperCase().replace(' ','_'));
            GLASS_COLORS.put(team, glass);
            Material concrete = Material.valueOf((blockColor + " concrete").toUpperCase().replace(' ','_'));
            CONCRETE_COLORS.put(team, concrete);

        }

    }

    //=========================================================================

    public static String GetTeamLogo(String team) {
        return TEAM_LOGOS.get(team);
    }

    public static TextComponent GetTeamMemberIcon(String team) {
        return TEAM_MEMBER.color(GetTextColor(team)).append(TextFormat.negativeSpace(9)).append(TEAM_MEMBER_OUTLINE);
    }

    public static TextColor GetTextColor(String team) {

        if (CHAT_COLORS.get(team) != null) {
            return CHAT_COLORS.get(team);
        }
        return DEFAULT_CHAT_COLOR;

    }

    public static NamedTextColor GetNamedColor(String team) {

        if (NAMED_COLORS.get(team) != null) {
            return NAMED_COLORS.get(team);
        }
        return DEFAULT_NAMED_COLOR;

    }

    public static org.bukkit.Color GetColor(String team) {

        if (UI_COLORS.get(team) != null) {
            return UI_COLORS.get(team);
        }
        return DEFAULT_UI_COLOR;

    }

    public static Integer GetIntColor(String team) {

        if (INT_COLORS.get(team) != null) {
            return INT_COLORS.get(team);
        }
        return DEFAULT_INT_COLOR;

    }

    public static TextComponent FormatTeamName(String teamName) {

        return AddTeamColor(teamName, teamName);

    }

    public static TextComponent FormatIgn(String ign) {

        String team = TeamManager.getTeam(ign);
        return AddTeamColor(team, ign);

    }

    private static TextComponent AddTeamColor(String team, String text) {

        return Component.text(text).color(GetTextColor(team));

    }

    public static Material glassColor(String team) {
        return GLASS_COLORS.get(team);
    }

    public static Material concreteColor(String team) {
        return CONCRETE_COLORS.get(team);
    }

    //=========================================================================

    // Alphanumeric (normal + small text) and :, |, !, @, _, », «, space, period
    // Also works for negative space and background characters
    public static int getTextWidth(TextComponent component) {

        List<Component> allComponents = new ArrayList<>(getAllChildren(component));
        allComponents.add(component);

        int min = 0;
        int max = 0;
        int currentPos = 0;

        for (Component child : allComponents) {

            if (child instanceof TextComponent textChild) {

                currentPos += finalTextPos(textChild.content());

                if (currentPos > max) max = currentPos;
                if (currentPos < min) min = currentPos;

            }

        }

        return max-min;

    }

    private static List<Component> getAllChildren(Component component) {

        List<Component> allChildren = new ArrayList<>(component.children());
        for (Component child : component.children()) {
            allChildren.addAll(getAllChildren(child));
        }
        return allChildren;

    }

    public static int getTextWidth(String str) {

        int min = 0;
        int max = 0;
        int currentPos = 0;

        for (char c : str.toCharArray()) {

            currentPos += charWidth(c);

            if (currentPos > max) max = currentPos;
            if (currentPos < min) min = currentPos;

        }

        return max - min;

    }

    // Horizontal distance (px) from start to end of a string
    public static int finalTextPos(String str) {

        int currentPos = 0;

        for (char c : str.toCharArray()) {

            currentPos += charWidth(c);

        }

        return currentPos;

    }

    private static int charWidth(char c) {

        //noinspection EnhancedSwitchMigration
        switch (c) {

            case 'I':
            case 't':
            case ' ':
            case 'ɪ':
            case '₁':
            case bg3px:
                return 4;

            case 'k':
            case 'f':
            case '₂':
            case '₃':
            case '₄':
            case '₅':
            case '₆':
            case '₇':
            case '₈':
            case '₉':
            case '₀':
                return 5;

            case 'i':
            case '.':
            case '!':
            case ':':
            case '|':
            case '\uE00B':
                return 2;

            case 'l':
            case '\uE009':
            case '\uE00A':
                return 3;

            case '»':
            case '«':
            case '@':
                return 7;

            case bg8px:

                // Team logos
            case '\uE020':
            case '\uE021':
            case '\uE022':
            case '\uE023':
            case '\uE024':
            case '\uE025':
            case '\uE026':
            case '\uE027':
            case '\uE028':
            case '\uE029':
            case '\uE02A':

                // Team member icons
            case '\uE040':
            case '\uE041':
                return 9;

            case px1:
                return 1;

            case neg1:
                return -1;

            case neg6:
                return -6;

            default:
                return 6;

        }

    }

    public static TextComponent backgroundString(int pixels) {

        String str;
        if (pixels == 0) {
            str = "";
        } else {

            if (pixels < 14) {

                pixels = switch (pixels) {
                    default -> 14;
                    case 10 -> 11;
                    case 7 -> 8;
                    case 5 -> 6;
                    case 4, 2, 1 -> 3;
                };

            }

            str = switch (pixels % 8) {

                default -> EIGHT_PX_BACKGROUND.repeat(pixels / 8);
                case 1 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8 - 1) + THREE_PX_BACKGROUND.repeat(3);
                case 2 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8 - 2) + THREE_PX_BACKGROUND.repeat(6);
                case 3 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8) + THREE_PX_BACKGROUND;
                case 4 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8 - 1) + THREE_PX_BACKGROUND.repeat(4);
                case 5 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8 - 2) + THREE_PX_BACKGROUND.repeat(7);
                case 6 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8) + THREE_PX_BACKGROUND.repeat(2);
                case 7 -> EIGHT_PX_BACKGROUND.repeat(pixels / 8 - 1) + THREE_PX_BACKGROUND.repeat(5);

            };

        }

        return Component.text(str).color(NO_SHADOW);

    }

    public static TextComponent padSpaces(int pixels) {

        String str;
        if (pixels == 0) {
            str = "";
        } else {
            str = " ".repeat(pixels / 4) + PLUS_ONE_PX.repeat(pixels % 4);
        }
        return Component.text(str);

    }

    public static TextComponent negativeSpace(int pixels) {

        String str;
        if (pixels == 0) {
            str = "";
        } else {
            str = NEGATIVE_SIX_PX.repeat(pixels / 6) + NEGATIVE_ONE_PX.repeat(pixels % 6);
        }
        return Component.text(str);

    }

    public static String singleLine(int pixels) {

        return repeatToWidth(pixels, '-');

    }

    public static String doubleLine(int pixels) {

        return repeatToWidth(pixels, '=');

    }

    private static String repeatToWidth(int pixels, char c) {

        String px5 = c + NEGATIVE_ONE_PX;

        int multiples = pixels/5;
        int remaining = pixels - 5*multiples;

        String end = NEGATIVE_ONE_PX.repeat(5-remaining) + c;

        return px5.repeat(multiples) + end;

    }

    public static String formatNumber(double num) {

        String rounded = String.format("%.1f", num);
        if (rounded.endsWith(".0")) {
            return String.format("%.0f", num);
        } else {
            return rounded;
        }

    }

    public static String formatPlace(int place) {

        if (place == 11) return "11th";
        if (place%10 == 1) return place+"st";
        if (place == 12) return "12th";
        if (place%10 == 2) return place+"nd";
        if (place == 13) return "13th";
        if (place%10 == 3) return place+"rd";
        return place+"th";

    }

    public static String smallText(String input) {

        return input.toLowerCase()
                    .replace('a','ᴀ')
                    .replace('b','ʙ')
                    .replace('c','ᴄ')
                    .replace('d','ᴅ')
                    .replace('e','ᴇ')
                    .replace('f','ғ')
                    .replace('g','ɢ')
                    .replace('h','ʜ')
                    .replace('i','ɪ')
                    .replace('j','ᴊ')
                    .replace('k','ᴋ')
                    .replace('l','ʟ')
                    .replace('m','ᴍ')
                    .replace('n','ɴ')
                    .replace('o','ᴏ')
                    .replace('p','ᴘ')
                    .replace('q','ǫ')
                    .replace('r','ʀ')
                    .replace('s','\uE007')
                    .replace('t','ᴛ')
                    .replace('u','ᴜ')
                    .replace('v','ᴠ')
                    .replace('w','ᴡ')
                    .replace('x','\uE008')
                    .replace('y','ʏ')
                    .replace('z','ᴢ')

                    .replace('1','₁')
                    .replace('2','₂')
                    .replace('3','₃')
                    .replace('4','₄')
                    .replace('5','₅')
                    .replace('6','₆')
                    .replace('7','₇')
                    .replace('8','₈')
                    .replace('9','₉')
                    .replace('0','₀')

                    .replace('[','\uE009')
                    .replace(']','\uE00A')
                    .replace(':','\uE00B');

    }

    //=========================================================================

}
