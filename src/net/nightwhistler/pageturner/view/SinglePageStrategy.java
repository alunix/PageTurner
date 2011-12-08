package net.nightwhistler.pageturner.view;

import net.nightwhistler.pageturner.epub.PageTurnerSpine;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.TextView;


public class SinglePageStrategy implements PageChangeStrategy {
	
	private Spanned text = new SpannableString("");	
	private int storedPosition = - 1;
	
	private BookView bookView;
	private TextView childView;
	
	private static final int MAX_PAGE_SIZE = 2048;
	
	public SinglePageStrategy(BookView bookView) {
		this.bookView = bookView;
		this.childView = bookView.getInnerView();			
	}

	@Override
	public int getPosition() {
		//Hack
		if ( this.storedPosition == Integer.MAX_VALUE ) {
			return 0;
		}
		
		return this.storedPosition;
	}
	
	@Override
	public void loadText(Spanned text) {
		this.text = text;	
		updatePosition();
	}
	
	@Override
	public void pageDown() {			
		
		int totalLength = this.text.length();
		
		int textEnd = this.storedPosition + 
			this.childView.getText().length();
		
		if ( textEnd >= text.length() -1 ) {
			PageTurnerSpine spine = bookView.getSpine();
			
			if ( spine == null || ! spine.navigateForward() ) {
				return;
			}
			
			this.storedPosition = 0;
			this.childView.setText("");
			bookView.loadText();
			return;
		}
		
		this.storedPosition = Math.min(textEnd, totalLength -1 ); 
		
		
		updatePosition();
	}

	private int findStartOfPage( int endOfPageOffset ) {
		
		int endOffset = endOfPageOffset;
		
		endOffset = Math.max(0, endOffset);
		endOffset = Math.min(this.text.length() -1, endOffset);
				
		int start = Math.max(0, endOffset - MAX_PAGE_SIZE);
		
		CharSequence cutOff = this.text.subSequence(start, endOffset);		

		TextPaint textPaint = childView.getPaint();
		int boundedWidth = childView.getWidth();
		StaticLayout layout = new StaticLayout(cutOff, textPaint, boundedWidth , Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		layout.draw(new Canvas());	
		
		if ( layout.getHeight() < bookView.getHeight() ) {
			return start;
		} else {
				
			int topLine = layout.getLineForVertical( layout.getHeight() - (bookView.getHeight() - 2 * BookView.PADDING ) );
			int offset = layout.getLineStart( topLine +2 );
		
			return start + offset;
		}
	}
	
	@Override
	public void pageUp() {				
		
		int pageStart = findStartOfPage(this.storedPosition);		
		
		if ( pageStart == this.storedPosition ) {
			if ( bookView.getSpine() == null || ! bookView.getSpine().navigateBack() ) {
				return;
			}
			
			this.childView.setText("");
			this.storedPosition = Integer.MAX_VALUE;
			this.bookView.loadText();
			return;
		} else {
			this.storedPosition = pageStart;
		}				
		
		updatePosition();
	}
	
	
	private void updatePosition() {	
		
		if ( this.text.length() == 0 ) {
			return;
		}
		
		if ( this.storedPosition >= text.length() ) {
			this.storedPosition = findStartOfPage( text.length() -1 );
		}
		
		this.storedPosition = Math.max(0, this.storedPosition);
		this.storedPosition = Math.min(this.text.length() -1, this.storedPosition);
				
		
		int totalLength = this.text.length();
		int end = Math.min( storedPosition + MAX_PAGE_SIZE, totalLength);
		
		CharSequence cutOff = this.text.subSequence(storedPosition, end ); 
	
		TextPaint textPaint = childView.getPaint();
		int boundedWidth = childView.getWidth();

		StaticLayout layout = new StaticLayout(cutOff, textPaint, boundedWidth , Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		layout.draw(new Canvas());
					
		int bottomLine = layout.getLineForVertical( bookView.getHeight() - ( 2 * BookView.PADDING) );
		bottomLine = Math.max( 1, bottomLine );
		
		if ( layout.getHeight() >= bookView.getHeight() && text.length() > 10) {
			
			int offset = layout.getLineStart(bottomLine -1);		
			CharSequence section = cutOff.subSequence(0, offset);		
			childView.setText(section);
			
		} else {
			childView.setText(cutOff);
		}
	}
	
	@Override
	public void setPosition(int pos) {
		this.storedPosition = pos;
		
		updatePosition();
	}
	
	@Override
	public boolean isScrolling() {		
		return false;
	}
	
}
