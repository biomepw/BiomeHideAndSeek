package pw.biome.hideandseek.objects;

import lombok.Getter;
import pw.biome.hideandseek.util.TeamType;

import java.util.ArrayList;
import java.util.List;

public class HSTeam {

    @Getter
    private final String name;

    @Getter
    private final TeamType teamType;

    @Getter
    private final List<HSPlayer> members = new ArrayList<>();

    public HSTeam(String name, TeamType teamType) {
        this.name = name;
        this.teamType = teamType;
    }

    public void addPlayer(HSPlayer hsPlayer) {
        members.add(hsPlayer);
    }

    public void removePlayer(HSPlayer hsPlayer) {
        members.remove(hsPlayer);
    }
}
