package towerdefense.ashley.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import towerdefense.ashley.components.RemoveComponent
import towerdefense.ashley.components.TransformComponent
import towerdefense.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import towerdefense.V_WORLD_HEIGHT_UNITS
import towerdefense.V_WORLD_WIDTH_UNITS
import towerdefense.ashley.components.GraphicComponent
import towerdefense.ashley.components.MoveComponent
import towerdefense.ashley.findComponent

private const val VER_ACCELERATION = 2.25f
private const val HOR_ACCELERATION = 16.5f
private const val MAX_VER_NEG_PLAYER_SPEED = 0.75f
private const val MAX_VER_POS_PLAYER_SPEED = 5f
private const val MAX_HOR_SPEED = 5.5f
private const val UPDATE_RATE = 1 / 25f

@Deprecated("Не разбирался с этой штукой")
class MoveSystem(
    private val gameEventManager: GameEventManager
) : IteratingSystem(allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class).get()) {
    private var accumulator = 0f

    override fun update(deltaTime: Float) {
        accumulator += deltaTime
        while (accumulator >= UPDATE_RATE) {
            accumulator -= UPDATE_RATE

            // store position before updating
            entities.forEach { entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    transform.prevPosition.set(transform.position)
                }
            }

            super.update(UPDATE_RATE)
        }

        val alpha = accumulator / UPDATE_RATE
        // update interpolation position
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                transform.interpolatedPosition.set(
                    MathUtils.lerp(transform.prevPosition.x, transform.position.x, alpha),
                    MathUtils.lerp(transform.prevPosition.y, transform.position.y, alpha),
                    transform.position.z
                )
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transformComp = entity.findComponent(TransformComponent.mapper)
        val moveComp = entity.findComponent(MoveComponent.mapper)

//        val player = entity[PlayerComponent.mapper]
//        if (player != null) {
//            entity[FacingComponent.mapper]?.let { facing ->
//                movePlayer(transform, move, player, facing, deltaTime)
//            }
//        } else {
            moveEntity(transformComp, moveComp, deltaTime)
//        }
    }

//    private fun movePlayer(
//        transform: TransformComponent,
//        move: MoveComponent,
//        player: PlayerComponent,
//        facing: FacingComponent,
//        deltaTime: Float
//    ) {
//        // update horizontal move speed
//        move.speed.x = when (facing.direction) {
//            FacingDirection.LEFT -> min(0f, move.speed.x - HOR_ACCELERATION * deltaTime)
//            FacingDirection.RIGHT -> max(0f, move.speed.x + HOR_ACCELERATION * deltaTime)
//            else -> 0f
//        }
//        move.speed.x = MathUtils.clamp(move.speed.x, -MAX_HOR_SPEED, MAX_HOR_SPEED)
//
//        // update vertical move speed
//        move.speed.y = MathUtils.clamp(
//            move.speed.y - VER_ACCELERATION * deltaTime,
//            -MAX_VER_NEG_PLAYER_SPEED,
//            MAX_VER_POS_PLAYER_SPEED
//        )
//
//        // move player and update distance travelled so far
//        val oldY = transform.position.y
//        moveEntity(transform, move, deltaTime)
//        player.distance += abs(transform.position.y - oldY)
//        gameEventManager.dispatchEvent(GameEvent.PlayerMove.apply {
//            distance = player.distance
//            speed = move.speed.y
//        })
//    }

    private fun moveEntity(
            transform: TransformComponent,
            move: MoveComponent,
            deltaTime: Float
    ) {
        transform.position.x = MathUtils.clamp(
            transform.position.x + move.speed.x * deltaTime,
            0f,
            V_WORLD_WIDTH_UNITS - transform.size.x
        )
        transform.position.y = MathUtils.clamp(
            transform.position.y + move.speed.y * deltaTime,
            1f,
            V_WORLD_HEIGHT_UNITS + 1f - transform.size.y
        )
    }
}
