package com.flat20.gui.widgets;

import com.flat20.fingerplay.config.IConfigUpdateListener;
import com.flat20.fingerplay.config.dto.ConfigItem;
import com.flat20.fingerplay.config.dto.ConfigLayout;
import com.flat20.fingerplay.config.dto.ConfigScreen;
import com.flat20.gui.animations.Animation;
import com.flat20.gui.animations.AnimationManager;
import com.flat20.gui.animations.Slide;
import com.flat20.gui.sprites.Sprite;
import com.flat20.gui.widgets.Scrollbar.IScrollable;

/**
 * Expands after added content size.
 * 
 * @author andreas
 *
 */
public class MidiWidgetContainer extends WidgetContainer implements IScrollable, IConfigUpdateListener {

	private int mScreenWidth;
	private int mScreenHeight;

	final private AnimationManager mAnimationManager;

	// Slide animation when you click the navigation buttons.
    private Slide mSlide = null;

	private IScrollListener mScrollListener;

	public MidiWidgetContainer(int screenWidth, int screenHeight) {
		super(0, 0);
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mAnimationManager = AnimationManager.getInstance();

		mSlide = new Slide(this, 0, y);
	}

	/**
	 * Goes through the ConfigLayout and instantiates any ConfigItems in it.
	 * @param layout
	 */
	@Override
	public void onConfigUpdated(ConfigLayout layout) {
		super.mSprites = new Sprite[0];
		super.mWidgets = new Widget[0];
		super.mNumWidgets = 0;
		super.mNumSprites = 0;
		height = 0;
		width = 0;
		// Scale values if layout wasn't exactly the right size.
		float scaleX = mScreenWidth / (float)layout.width;
		float scaleY = mScreenHeight / (float)layout.height;
	
		for (ConfigScreen screen : layout.screens) {
	
			int screenX = (int)(screen.x * scaleX);
			int screenY = (int) (screen.y * scaleY);
			int screenWidth = (int) (screen.width * scaleX);
			int screenHeight = (int) (screen.height * scaleY);
	
			WidgetContainer wc = new WidgetContainer(screenWidth, screenHeight);
			wc.x = screenX;
			wc.y = screenY;
	
	    	for (ConfigItem configItem : screen.items) {

				try {/*
					Class<?> WidgetClass = Class.forName( configItem.viewClassName );
					Class<?>[] classParams = new Class<?>[] {IMidiController.class};
					Object[] objectParams = new Object[] { (IMidiController) configItem.itemController };
					Constructor<?> ctor = WidgetClass.getConstructor( classParams );

					Widget widget = (Widget) ctor.newInstance(objectParams);
					*/
					Widget widget = null;
					// We only want the views of type Widget for now.
					if (configItem.itemView instanceof Widget)
						widget = (Widget) configItem.itemView;

					if (widget != null) {

						int widgetWidth = (int)(configItem.width * scaleX);
						int widgetHeight = (int)(configItem.height * scaleY);

						widget.x = (int)(configItem.x * scaleX);
						widget.y = (int)(configItem.y * scaleY);
						widget.setSize(widgetWidth, widgetHeight);
						wc.addSprite(widget);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

	    	}

	    	addSprite( wc );
		}
    }



	/**
	 * Pauses our internal drag animation and slides to destY
	 * @param destY
	 */
	@Override
	public void scrollTo(int destY) {
		mSlide.set(0, destY);

		if (!mAnimationManager.hasAnimation(mSlide));
			mAnimationManager.add( mSlide );
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setUpdateListener(IScrollListener listener) {
		mScrollListener = listener;
	}

	/*
	 * Adds the Sprite to the list and expands width and height.
	 */
	@Override
	public void addSprite(Sprite sprite) {
		super.addSprite(sprite);

		if (sprite.y + sprite.height > height) {
			height = sprite.y + sprite.height;
		}

		if (sprite.x + sprite.width > width) {
			width = sprite.x + sprite.width;
		}
	}

	public void setY(int newY) {
		y = Math.max(-(this.height-mScreenHeight), Math.min(0, newY));
		//Log.i("MWC", "setY = " + y + " height = " + this.height + ", " + Renderer.VIEWPORT_HEIGHT);
		if (mScrollListener != null)
			mScrollListener.onScrollChanged(y);
	}

	@Override
	public boolean onTouchDown(int touchX, int touchY, float pressure, int pointerId) {
		super.onTouchDown(touchX, touchY, pressure, pointerId);
		return true;
	}

	@Override 
	public boolean onTouchMove(int touchX, int touchY, float pressure, int pointerId) {
		return super.onTouchMove(touchX, touchY, pressure, pointerId);
	}

	@Override
	public boolean onTouchUp(int touchX, int touchY, float pressure, int pointerId) {
		return super.onTouchUp(touchX, touchY, pressure, pointerId);
	}

	class DragAnimation extends Animation {

		private MidiWidgetContainer mContainer;

		public float velocityY = 0;
		public float y = 0;
		public boolean isRunning = true;

		/**
		 * A neverending animation for our MidiWidgetContainer 
		 * 
		 * @param sprite
		 * @param destX
		 * @param destY
		 */
		public DragAnimation(MidiWidgetContainer container) {
			mContainer = container;
		}

		@Override
		public boolean update() {
			if (isRunning) {
				if (Math.abs(velocityY) < 0.1) {
					isRunning = false;
				} else {
					y += velocityY;
					mContainer.setY( (int)y );
					velocityY *= 0.9f;
				}
				
			}
			return true;
		}

	}

}