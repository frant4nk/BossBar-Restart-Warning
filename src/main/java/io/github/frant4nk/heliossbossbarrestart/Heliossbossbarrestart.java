package io.github.frant4nk.heliossbossbarrestart;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "heliossbossbarrestart",
        name = "Heliossbossbarrestart",
        version = "1.0",
        description = "A plugin to show the remaining time of a restart with a boss bar"
)
public class Heliossbossbarrestart {

    @Inject
    private Logger logger;

    @Inject
    Game game;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Starting boss bar plugin...");

        Task.Builder task = Task.builder();
        int secondToRestart = getTimeLeft();
        logger.info("Restarting in " + secondToRestart + " seconds");
        //Task.Builder stopServer = Task.builder();
        ServerBossBar bar = ServerBossBar.builder()
                .color(BossBarColors.BLUE)
                .name(Text.of("Restarting the server in 20s"))
                .overlay(BossBarOverlays.PROGRESS)
                .percent(1.0f)
                .build();
        final SpongeExecutorService.SpongeFuture<?>[] bossBarUpdater = new SpongeExecutorService.SpongeFuture<?>[1];
        task.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Boss bar counter started");
                Collection<Player> onlinePlayers = game.getServer().getOnlinePlayers();
                bar.addPlayers(onlinePlayers);
                bossBarUpdater[0] = game.getScheduler().createAsyncExecutor(game.getPluginManager().getPlugin("heliossbossbarrestart").get().getInstance().get()).scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Collection<Player> onlinePlayers = game.getServer().getOnlinePlayers();
                        bar.setPercent(bar.getPercent() - (1.0f / 20)); //20 is the warning time left before the restart
                        bar.setName(Text.of("Restarting in ", Math.round(20 * bar.getPercent()), " seconds"));
                        bar.addPlayers(onlinePlayers);

                        for (Player player : onlinePlayers) {
                            player.playSound(SoundTypes.BLOCK_NOTE_BELL, player.getLocation().getPosition(), 25);
                        }
                    }
                }, 0, 1, TimeUnit.SECONDS);
            }
        }).async().delay(secondToRestart - 20, TimeUnit.SECONDS); //6 hours --> 21600 seconds minus warning time
        task.submit(this);
        /*
        stopServer.execute(new Runnable() {
            @Override
            public void run() {
                ConsoleSource source = game.getServer().getConsole();
                game.getCommandManager().process(source, "stop");
            }
        }).async().delay(31, TimeUnit.SECONDS);
        stopServer.submit(this);
        */
    }

    public static int getTimeLeft()
    {
        int[] timesArray = new int[]{21530, 43130, 64730, 86330}; //05:58:50, 11:58:50, 17:58:50, 23:58:50

        LocalDateTime localDate = LocalDateTime.now();
        int hours = localDate.getHour();
        int minutes = localDate.getMinute();
        int seconds = localDate.getSecond();

        int actual_time_in_seconds = (hours * 3600) + (minutes * 60) + seconds;

        int aux = 0;
        int diff = Integer.MAX_VALUE;
        for (int i = 0; i < timesArray.length; i++)
        {
            aux = timesArray[i] - actual_time_in_seconds;
            if (aux > 0 && aux < diff)
            {
                diff = aux;
            }
        }
        return diff;
    }
}
