package com.petopia.battle;

import com.petopia.model.Combatant;
import java.util.Random;
import java.util.function.Consumer;

public class BattleService {

    private final Combatant player;
    private final Combatant enemy;
    private final Consumer<BattleEvent> eventConsumer;
    private final Random rnd = new Random();

    private boolean battleOver = false;
    private boolean playerFled = false;
    private int potionCount = 1; // default; bisa di-pass/diubah

    public BattleService(Combatant player, Combatant enemy, Consumer<BattleEvent> eventConsumer) {
        this.player = player;
        this.enemy = enemy;
        this.eventConsumer = eventConsumer;
    }

    public void start() {
        event("BATTLE START! " + player.getName() + " vs " + enemy.getName() + "!");
        updateHpAll();
    }

    // Player actions:
    public void playerAttack() {
        if (battleOver) return;
        // gunakan computeAttackValue() (polymorphic) agar player/ability bisa override behavior
        int dmg = calcDamage(player.computeAttackValue(), enemy.getDefense());
        int dealt = enemy.takeDamage(dmg);
        event(player.getName() + " attacks for " + dealt + " damage!");
        sendEvent(BattleEvent.hpUpdate(1, enemy.getHp(), enemy.getMaxHp()));
        checkAfterPlayerAction();
    }

    public void playerUsePotion() {
        if (battleOver) return;
        if (potionCount <= 0) {
            event("No potions left!");
            enemyTurnIfAlive();
            return;
        }
        potionCount--;
        int heal = Math.min(30, player.getMaxHp() - player.getHp());
        player.heal(30);
        event(player.getName() + " used Health Potion! Restored " + heal + " HP!");
        sendEvent(BattleEvent.hpUpdate(0, player.getHp(), player.getMaxHp()));
        checkAfterPlayerAction(); // enemy may act next
    }

    public void playerFlee() {
        if (battleOver) return;
        // success chance e.g. 50%
        boolean success = rnd.nextDouble() < 0.5;
        if (success) {
            playerFled = true;
            battleOver = true;
            event(player.getName() + " fled successfully!");
            sendEvent(BattleEvent.end(player.getName() + " fled from battle."));
        } else {
            event(player.getName() + " failed to flee!");
            enemyTurnIfAlive();
        }
    }

    // internal helpers
    private int calcDamage(int atk, int def) {
        // simple formula: base = atk - def/2; plus randomness
        int base = Math.max(1, atk - def/2);
        int variance = rnd.nextInt(Math.max(1, base)); // 0..base-1
        return base + variance;
    }

    private void enemyTurnIfAlive() {
        if (enemy.isFainted() || battleOver) return;
        // Signal UI to play enemy lunge animation
        sendEvent(BattleEvent.enemyAttack());
        // gunakan computeAttackValue() secara polymorphic
        int enemyAtk = enemy.computeAttackValue();
        int dmg = calcDamage(enemyAtk, player.getDefense());
        int dealt = player.takeDamage(dmg);
        event(enemy.getName() + " attacks for " + dealt + " damage!");
        sendEvent(BattleEvent.hpUpdate(0, player.getHp(), player.getMaxHp()));
        checkBattleEnd();
    }

    private void checkAfterPlayerAction() {
        if (enemy.isFainted()) {
            battleOver = true;
            event(enemy.getName() + " fainted!");
            sendEvent(BattleEvent.end(player.getName() + " wins!"));
            return;
        }
        // enemy gets a turn
        enemyTurnIfAlive();
    }

    private void checkBattleEnd() {
        if (player.isFainted()) {
            battleOver = true;
            event(player.getName() + " fainted!");
            sendEvent(BattleEvent.end(enemy.getName() + " wins!"));
        }
    }

    // 2 methods untuk mengirim event:
    // untuk log text (String)
    private void event(String msg) {
        if (eventConsumer != null) {
            eventConsumer.accept(BattleEvent.log(msg));
        }
    }

    // untuk event lain (BattleEvent)
    private void sendEvent(BattleEvent ev) {
        if (eventConsumer != null) {
            eventConsumer.accept(ev);
        }
    }

    private void updateHpAll() {
        if (eventConsumer != null) {
            eventConsumer.accept(BattleEvent.hpUpdate(0, player.getHp(), player.getMaxHp()));
            eventConsumer.accept(BattleEvent.hpUpdate(1, enemy.getHp(), enemy.getMaxHp()));
        }
    }
}