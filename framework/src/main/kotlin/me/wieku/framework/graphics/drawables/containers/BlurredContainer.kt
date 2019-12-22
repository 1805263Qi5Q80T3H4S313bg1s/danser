package me.wieku.framework.graphics.drawables.containers

import me.wieku.framework.graphics.drawables.sprite.Sprite
import me.wieku.framework.graphics.drawables.sprite.SpriteBatch
import me.wieku.framework.graphics.effects.BlurEffect
import me.wieku.framework.math.view.Camera

open class BlurredContainer() : Container() {

    private val blur = BlurEffect(1920, 1080)
    private val tempSprite = Sprite()

    var needsRedraw = true

    var blurAmount = 0.0f
        set(value) {
            blur.setBlur(value, value)
            invalidate()
            field = value
        }

    private var needsResize = true

    private var camera = Camera()

    constructor(inContext: BlurredContainer.() -> Unit) : this() {
        inContext()
    }

    init {
        blur.setBlur(blurAmount, blurAmount)
    }

    override fun update() {
        super.update()
        camera.setViewportF(drawPosition.x.toInt(), drawPosition.y.toInt(), drawSize.x.toInt(), drawSize.y.toInt())
        camera.update()
        if (wasUpdated) {
            needsRedraw = true
        }
        if ((drawSize.x.toInt() != blur.width || drawSize.y.toInt() != blur.height) && !needsResize) {
            needsRedraw = true
            needsResize = true
        }
    }

    override fun updateDrawable() {
        super.updateDrawable()
        tempSprite.apply {
            drawPosition = this@BlurredContainer.drawPosition
            drawOrigin = this@BlurredContainer.drawOrigin
            drawSize = this@BlurredContainer.drawSize
            drawColor = this@BlurredContainer.drawColor
            shearX = this@BlurredContainer.shearX
            shearY = this@BlurredContainer.shearY
            additive = this@BlurredContainer.additive
            flipX = this@BlurredContainer.flipX
            flipY = this@BlurredContainer.flipY
        }
    }

    override fun draw(batch: SpriteBatch) {
        if (needsResize) {
            blur.resize(drawSize.x.toInt(), drawSize.y.toInt())
            needsResize = false
        }

        if (needsRedraw) {
            batch.flush()
            blur.begin()
            val oldCamera = batch.camera
            batch.camera = camera
            super.draw(batch)
            batch.flush()
            tempSprite.apply {
                texture = blur.endAndProcess().region
            }
            batch.camera = oldCamera
            needsRedraw = false
        }

        batch.draw(tempSprite)
    }

}