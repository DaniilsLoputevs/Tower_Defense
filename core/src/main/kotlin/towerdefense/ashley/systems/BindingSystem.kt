package towerdefense.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.ashley.contains
import towerdefense.CARD_STACK_OFFSET
import towerdefense.ashley.components.game.GameCardComponent
import towerdefense.ashley.components.RemoveComponent
import towerdefense.ashley.components.TransformComponent
import towerdefense.ashley.components.game.GameStacksComponent
import towerdefense.ashley.findRequiredComponent
import towerdefense.gameStrucures.DragAndDropManager.DragAndDropStatus.*
import towerdefense.gameStrucures.GameContext

class BindingSystem(
) : IteratingSystem(allOf(GameCardComponent::class).exclude(RemoveComponent::class.java).get()) {
    lateinit var gameContext: GameContext
    val coordinateContainer = Vector2(-1f, -1f)

    override fun update(deltaTime: Float) {
        if (gameContext.dndSelectedEntity != null
                && gameContext.dndEntityStatus == DROPPED) {
            processEntity(gameContext.dndSelectedEntity!!, deltaTime)
            gameContext.dndEntityStatus = NONE
            gameContext.dndSelectedEntity = null
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val entityTransComp = entity.findRequiredComponent(TransformComponent.mapper)
        for (stack in gameContext.stacks) {
            val stackComp = stack.findRequiredComponent(GameStacksComponent.mapper)
            if (stackComp.isEmpty()) continue

            val lastCard = stackComp.getLastCard()
            val stackLastCardTransComp = lastCard.findRequiredComponent(TransformComponent.mapper)

            if (stackLastCardTransComp.shape.contains(entityTransComp.shape.getCenter(coordinateContainer))) {
                val entityGameCardComp = entity.findRequiredComponent(GameCardComponent.mapper)
                val stackLastGameCardComp = lastCard.findRequiredComponent(GameCardComponent.mapper)

                if (!stackLastGameCardComp.setNextPredicate.evaluate(entityGameCardComp)) return
                deleteGameCardFromStack(entity)
                stackLastGameCardComp.next = entity
                stackComp.addGameCard(entity)

                val newPos = Vector2(stackLastCardTransComp.interpolatedPosition.x,
                        stackLastCardTransComp.interpolatedPosition.y - CARD_STACK_OFFSET,
                )
                entityTransComp.setTotalPosition(newPos)
                entityGameCardComp.moveNextCards(newPos)

//                gameContext.dndSelectedEntity = null
//                gameContext.dndEntityStatus = BINDING_SUCCESS
                // TODO - see AttachSystem in DarkMatter
            } else {
                returnGameCardToStack(entity)
//                gameContext.dndEntityStatus = BINDING_FAIL
            }
        }
    }

    private fun deleteGameCardFromStack(card : Entity) {
        gameContext.stacks.forEach {
            val stackComp = it.findRequiredComponent(GameStacksComponent.mapper)
            if (stackComp.contains(card)) stackComp.removeGameCard(card)
        }
    }
    private fun returnGameCardToStack(card : Entity) {
        gameContext.stacks.forEach {
            val stackComp = it.findRequiredComponent(GameStacksComponent.mapper)
            if (stackComp.contains(card)) {
                val entityTransComp = card.findRequiredComponent(TransformComponent.mapper)
                val entityGameCardComp = card.findRequiredComponent(GameCardComponent.mapper)
                val stackLastCardTransComp = stackComp.getLastCard().findRequiredComponent(TransformComponent.mapper)
                val newPos = Vector2(stackLastCardTransComp.interpolatedPosition.x,
                        stackLastCardTransComp.interpolatedPosition.y - CARD_STACK_OFFSET,
                )
                entityTransComp.setTotalPosition(newPos)
                entityGameCardComp.moveNextCards(newPos)
            }
        }
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
    }

}