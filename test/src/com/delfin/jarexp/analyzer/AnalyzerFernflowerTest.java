package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class AnalyzerFernflowerTest {

	@Test
	public void testDynamicIntArray() {
		String code = "public class DynamicIntArray implements Serializable {\r\n" + 
				"    private int[] data;\r\n" + 
				"    private int size;\r\n" + 
				"\r\n" + 
				"    public DynamicIntArray() {\r\n" + 
				"        this(10);// 42\r\n" + 
				"    }// 43\r\n" + 
				"\r\n" + 
				"    public DynamicIntArray(int initialCapacity) {\r\n" + 
				"        if (initialCapacity < 0) {// 54\r\n" + 
				"            throw new IllegalArgumentException(\"Illegal initialCapacity: \" + initialCapacity);// 55\r\n" + 
				"        } else {\r\n" + 
				"            this.data = new int[initialCapacity];// 58\r\n" + 
				"            this.size = 0;// 59\r\n" + 
				"        }\r\n" + 
				"    }// 60\r\n" + 
				"\r\n" + 
				"    public DynamicIntArray(int[] intArray) {\r\n" + 
				"        this.size = intArray.length;// 73\r\n" + 
				"        int capacity = (int)Math.min((long)this.size * 110L / 100L, 2147483647L);// 74\r\n" + 
				"        this.data = new int[capacity];// 75\r\n" + 
				"        System.arraycopy(intArray, 0, this.data, 0, this.size);// 76\r\n" + 
				"    }// 77\r\n" + 
				"\r\n" + 
				"    public void add(int value) {\r\n" + 
				"        this.ensureCapacity(this.size + 1);// 86\r\n" + 
				"        this.data[this.size++] = value;// 87\r\n" + 
				"    }// 88\r\n" + 
				"\r\n" + 
				"    public void add(int index, int[] intArray) {\r\n" + 
				"        if (index > this.size) {// 106\r\n" + 
				"            this.throwException2(index);// 107\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        int addCount = intArray.length;// 109\r\n" + 
				"        this.ensureCapacity(this.size + addCount);// 110\r\n" + 
				"        int moveCount = this.size - index;// 111\r\n" + 
				"        if (moveCount > 0) {// 112\r\n" + 
				"            System.arraycopy(this.data, index, this.data, index + addCount, moveCount);// 113\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        System.arraycopy(intArray, 0, this.data, index, addCount);// 115\r\n" + 
				"        this.size += addCount;// 116\r\n" + 
				"    }// 117\r\n" + 
				"\r\n" + 
				"    public void add(int index, int value) {\r\n" + 
				"        if (index > this.size) {// 133\r\n" + 
				"            this.throwException2(index);// 134\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        this.ensureCapacity(this.size + 1);// 136\r\n" + 
				"        System.arraycopy(this.data, index, this.data, index + 1, this.size - index);// 137\r\n" + 
				"        this.data[index] = value;// 138\r\n" + 
				"        ++this.size;// 139\r\n" + 
				"    }// 140\r\n" + 
				"\r\n" + 
				"    public void clear() {\r\n" + 
				"        this.size = 0;// 148\r\n" + 
				"    }// 149\r\n" + 
				"\r\n" + 
				"    public boolean contains(int integer) {\r\n" + 
				"        for(int i = 0; i < this.size; ++i) {// 160\r\n" + 
				"            if (this.data[i] == integer) {// 161\r\n" + 
				"                return true;// 162\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        return false;// 165\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public void decrement(int from, int to) {\r\n" + 
				"        for(int i = from; i < to; ++i) {// 177\r\n" + 
				"            int var10002 = this.data[i]--;// 178\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"    }// 180\r\n" + 
				"\r\n" + 
				"    private void ensureCapacity(int minCapacity) {\r\n" + 
				"        int oldCapacity = this.data.length;// 191\r\n" + 
				"        if (minCapacity > oldCapacity) {// 192\r\n" + 
				"            int[] oldData = this.data;// 193\r\n" + 
				"            int newCapacity = oldCapacity * 3 / 2 + 1;// 196\r\n" + 
				"            if (newCapacity < minCapacity) {// 197\r\n" + 
				"                newCapacity = minCapacity;// 198\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            this.data = new int[newCapacity];// 200\r\n" + 
				"            System.arraycopy(oldData, 0, this.data, 0, this.size);// 201\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"    }// 203\r\n" + 
				"\r\n" + 
				"    public void fill(int value) {\r\n" + 
				"        Arrays.fill(this.data, value);// 212\r\n" + 
				"    }// 213\r\n" + 
				"\r\n" + 
				"    public int get(int index) {\r\n" + 
				"        if (index >= this.size) {// 227\r\n" + 
				"            this.throwException(index);// 228\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        return this.data[index];// 230\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public int getUnsafe(int index) {\r\n" + 
				"        return this.data[index];// 244\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public int getSize() {\r\n" + 
				"        return this.size;// 254\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public void increment(int from, int to) {\r\n" + 
				"        for(int i = from; i < to; ++i) {// 266\r\n" + 
				"            int var10002 = this.data[i]++;// 267\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"    }// 269\r\n" + 
				"\r\n" + 
				"    public void insertRange(int offs, int count, int value) {\r\n" + 
				"        if (offs > this.size) {// 273\r\n" + 
				"            this.throwException2(offs);// 274\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        this.ensureCapacity(this.size + count);// 276\r\n" + 
				"        System.arraycopy(this.data, offs, this.data, offs + count, this.size - offs);// 277\r\n" + 
				"        if (value != 0) {// 278\r\n" + 
				"            Arrays.fill(this.data, offs, offs + count, value);// 279\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        this.size += count;// 281\r\n" + 
				"    }// 282\r\n" + 
				"\r\n" + 
				"    public boolean isEmpty() {\r\n" + 
				"        return this.size == 0;// 291\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public void remove(int index) {\r\n" + 
				"        if (index >= this.size) {// 304\r\n" + 
				"            this.throwException(index);// 305\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        int toMove = this.size - index - 1;// 307\r\n" + 
				"        if (toMove > 0) {// 308\r\n" + 
				"            System.arraycopy(this.data, index + 1, this.data, index, toMove);// 309\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        --this.size;// 311\r\n" + 
				"    }// 312\r\n" + 
				"\r\n" + 
				"    public void removeRange(int fromIndex, int toIndex) {\r\n" + 
				"        if (fromIndex >= this.size || toIndex > this.size) {// 326\r\n" + 
				"            this.throwException3(fromIndex, toIndex);// 327\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        int moveCount = this.size - toIndex;// 329\r\n" + 
				"        System.arraycopy(this.data, toIndex, this.data, fromIndex, moveCount);// 330\r\n" + 
				"        this.size -= toIndex - fromIndex;// 331\r\n" + 
				"    }// 332\r\n" + 
				"\r\n" + 
				"    public void set(int index, int value) {\r\n" + 
				"        if (index >= this.size) {// 346\r\n" + 
				"            this.throwException(index);// 347\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        this.data[index] = value;// 349\r\n" + 
				"    }// 350\r\n" + 
				"\r\n" + 
				"    public void setUnsafe(int index, int value) {\r\n" + 
				"        this.data[index] = value;// 363\r\n" + 
				"    }// 364\r\n" + 
				"\r\n" + 
				"    private void throwException(int index) {\r\n" + 
				"        throw new IndexOutOfBoundsException(\"Index \" + index + \" not in valid range [0-\" + (this.size - 1) + \"]\");// 381\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    private void throwException2(int index) {\r\n" + 
				"        throw new IndexOutOfBoundsException(\"Index \" + index + \", not in range [0-\" + this.size + \"]\");// 399\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    private void throwException3(int fromIndex, int toIndex) {\r\n" + 
				"        throw new IndexOutOfBoundsException(\"Index range [\" + fromIndex + \", \" + toIndex + \"] not in valid range [0-\" + (this.size - 1) + \"]\");// 417\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("DynamicIntArray()", methods.get(0).getName());
		assertEquals("DynamicIntArray(int)", methods.get(1).getName());
		assertEquals("DynamicIntArray(int[])", methods.get(2).getName());
		assertEquals("add(int) : void", methods.get(3).getName());
		assertEquals("add(int, int[]) : void", methods.get(4).getName());
		assertEquals("add(int, int) : void", methods.get(5).getName());
		assertEquals("clear() : void", methods.get(6).getName());
		assertEquals("contains(int) : boolean", methods.get(7).getName());
		assertEquals("decrement(int, int) : void", methods.get(8).getName());
		assertEquals("ensureCapacity(int) : void", methods.get(9).getName());
		assertEquals("fill(int) : void", methods.get(10).getName());
		assertEquals("get(int) : int", methods.get(11).getName());
		assertEquals("getUnsafe(int) : int", methods.get(12).getName());
		assertEquals("getSize() : int", methods.get(13).getName());
		assertEquals("increment(int, int) : void", methods.get(14).getName());
		assertEquals("insertRange(int, int, int) : void", methods.get(15).getName());
		assertEquals("isEmpty() : boolean", methods.get(16).getName());
		assertEquals("remove(int) : void", methods.get(17).getName());
		assertEquals("removeRange(int, int) : void", methods.get(18).getName());
		assertEquals("set(int, int) : void", methods.get(19).getName());
		assertEquals("setUnsafe(int, int) : void", methods.get(20).getName());
		assertEquals("throwException(int) : void", methods.get(21).getName());
		assertEquals("throwException2(int) : void", methods.get(22).getName());
		assertEquals("throwException3(int, int) : void", methods.get(23).getName());
	}

	@Test
	public void testAbstractJFlexCTokenMaker$CStyleInsertBreakAction() {
		String code = "\r\n" + 
				"public class AbstractJFlexCTokenMaker$CStyleInsertBreakAction extends InsertBreakAction {\r\n" + 
				"    protected AbstractJFlexCTokenMaker$CStyleInsertBreakAction(AbstractJFlexCTokenMaker this$0) {\r\n" + 
				"        this.this$0 = this$0;// 134\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {\r\n" + 
				"        if (textArea.isEditable() && textArea.isEnabled()) {// 140\r\n" + 
				"            RSyntaxTextArea rsta = (RSyntaxTextArea)this.getTextComponent(e);// 145\r\n" + 
				"            RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();// 146\r\n" + 
				"            int line = textArea.getCaretLineNumber();// 148\r\n" + 
				"            int type = doc.getLastTokenTypeOnLine(line);// 149\r\n" + 
				"            if (type < 0) {// 150\r\n" + 
				"                type = doc.getClosestStandardTokenTypeForInternalType(type);// 151\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            if (type != 3 && type != 2) {// 155\r\n" + 
				"                this.handleInsertBreak(rsta, true);// 160\r\n" + 
				"            } else {\r\n" + 
				"                this.insertBreakInMLC(e, rsta, line);// 157\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"        } else {\r\n" + 
				"            UIManager.getLookAndFeel().provideErrorFeedback(textArea);// 141\r\n" + 
				"        }\r\n" + 
				"    }// 142 163\r\n" + 
				"\r\n" + 
				"    private boolean appearsNested(RSyntaxTextArea textArea, int line, int offs) {\r\n" + 
				"        int firstLine = line;// 181\r\n" + 
				"\r\n" + 
				"        Token t;\r\n" + 
				"        do {\r\n" + 
				"            int i;\r\n" + 
				"            while(true) {\r\n" + 
				"                if (line >= textArea.getLineCount()) {// 183\r\n" + 
				"                    return true;// 210\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                t = textArea.getTokenListForLine(line);// 184\r\n" + 
				"                int i = false;// 185\r\n" + 
				"                if (line++ == firstLine) {// 187\r\n" + 
				"                    t = RSyntaxUtilities.getTokenAtOffset(t, offs);// 188\r\n" + 
				"                    if (t == null) {// 189\r\n" + 
				"                        continue;\r\n" + 
				"                    }\r\n" + 
				"\r\n" + 
				"                    i = t.documentToToken(offs);// 192\r\n" + 
				"                    break;\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                i = t.getTextOffset();// 195\r\n" + 
				"                break;\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            for(int textOffset = t.getTextOffset(); i < textOffset + t.length() - 1; ++i) {// 197 198 202\r\n" + 
				"                if (t.charAt(i - textOffset) == '/' && t.charAt(i - textOffset + 1) == '*') {// 199\r\n" + 
				"                    return true;// 200\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"        } while((t = t.getNextToken()) == null || AbstractJFlexCTokenMaker.access$000(this.this$0, t));// 205\r\n" + 
				"\r\n" + 
				"        return false;// 206\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    private void insertBreakInMLC(ActionEvent e, RSyntaxTextArea textArea, int line) {\r\n" + 
				"        Matcher m = null;// 217\r\n" + 
				"        int start = true;// 218\r\n" + 
				"        int end = true;// 219\r\n" + 
				"        String text = null;// 220\r\n" + 
				"\r\n" + 
				"        int start;\r\n" + 
				"        int end;\r\n" + 
				"        try {\r\n" + 
				"            start = textArea.getLineStartOffset(line);// 222\r\n" + 
				"            end = textArea.getLineEndOffset(line);// 223\r\n" + 
				"            text = textArea.getText(start, end - start);// 224\r\n" + 
				"            m = AbstractJFlexCTokenMaker.access$100().matcher(text);// 225\r\n" + 
				"        } catch (BadLocationException var14) {// 226\r\n" + 
				"            UIManager.getLookAndFeel().provideErrorFeedback(textArea);// 227\r\n" + 
				"            var14.printStackTrace();// 228\r\n" + 
				"            return;// 229\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        if (m.lookingAt()) {// 232\r\n" + 
				"            String leadingWS = m.group(1);// 234\r\n" + 
				"            String mlcMarker = m.group(2);// 235\r\n" + 
				"            int dot = textArea.getCaretPosition();// 239\r\n" + 
				"            boolean moved;\r\n" + 
				"            if (dot >= start && dot < start + leadingWS.length() + mlcMarker.length()) {// 240 241\r\n" + 
				"                if (mlcMarker.charAt(0) == '/') {// 244\r\n" + 
				"                    this.handleInsertBreak(textArea, true);// 245\r\n" + 
				"                    return;// 246\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                textArea.setCaretPosition(end - 1);// 248\r\n" + 
				"            } else {\r\n" + 
				"                for(moved = false; dot < end - 1 && Character.isWhitespace(text.charAt(dot - start)); ++dot) {// 254 255 256 258\r\n" + 
				"                    moved = true;// 257\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                if (moved) {// 260\r\n" + 
				"                    textArea.setCaretPosition(dot);// 261\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            moved = mlcMarker.charAt(0) == '/';// 265\r\n" + 
				"            boolean nested = this.appearsNested(textArea, line, start + leadingWS.length() + 2);// 266 267\r\n" + 
				"            String header = leadingWS + (moved ? \" * \" : \"*\") + m.group(3);// 268 270\r\n" + 
				"            textArea.replaceSelection(\"\\n\" + header);// 271\r\n" + 
				"            if (nested) {// 272\r\n" + 
				"                dot = textArea.getCaretPosition();// 273\r\n" + 
				"                textArea.insert(\"\\n\" + leadingWS + \" */\", dot);// 274\r\n" + 
				"                textArea.setCaretPosition(dot);// 275\r\n" + 
				"            }\r\n" + 
				"        } else {\r\n" + 
				"            this.handleInsertBreak(textArea, true);// 280\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"    }// 283\r\n" + 
				"}\r\n" + 
				"";
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();

		assertEquals("AbstractJFlexCTokenMaker$CStyleInsertBreakAction(AbstractJFlexCTokenMaker)", methods.get(0).getName());
		assertEquals("actionPerformedImpl(ActionEvent, RTextArea) : void", methods.get(1).getName());
		assertEquals("appearsNested(RSyntaxTextArea, int, int) : boolean", methods.get(2).getName());
		assertEquals("insertBreakInMLC(ActionEvent, RSyntaxTextArea, int) : void", methods.get(3).getName());
	}

	@Test
	public void testCodeTemplateManager() {
		String code = "public class CodeTemplateManager {\r\n" + 
				"    private int maxTemplateIDLength;\r\n" + 
				"    private List<CodeTemplate> templates = new ArrayList();\r\n" + 
				"    private Segment s = new Segment();\r\n" + 
				"    private org.fife.ui.rsyntaxtextarea.CodeTemplateManager.TemplateComparator comparator = new org.fife.ui.rsyntaxtextarea.CodeTemplateManager.TemplateComparator((1)null);\r\n" + 
				"    private File directory;\r\n" + 
				"\r\n" + 
				"    public synchronized void addTemplate(CodeTemplate template) {\r\n" + 
				"        if (template == null) {// 82\r\n" + 
				"            throw new IllegalArgumentException(\"template cannot be null\");// 83\r\n" + 
				"        } else {\r\n" + 
				"            this.templates.add(template);// 85\r\n" + 
				"            this.sortTemplates();// 86\r\n" + 
				"        }\r\n" + 
				"    }// 87\r\n" + 
				"\r\n" + 
				"    public synchronized CodeTemplate getTemplate(RSyntaxTextArea textArea) {\r\n" + 
				"        int caretPos = textArea.getCaretPosition();// 99\r\n" + 
				"        int charsToGet = Math.min(caretPos, this.maxTemplateIDLength);// 100\r\n" + 
				"\r\n" + 
				"        try {\r\n" + 
				"            Document doc = textArea.getDocument();// 102\r\n" + 
				"            doc.getText(caretPos - charsToGet, charsToGet, this.s);// 103\r\n" + 
				"            int index = Collections.binarySearch(this.templates, this.s, this.comparator);// 105\r\n" + 
				"            return index >= 0 ? (CodeTemplate)this.templates.get(index) : null;// 106\r\n" + 
				"        } catch (BadLocationException var6) {// 107\r\n" + 
				"            var6.printStackTrace();// 108\r\n" + 
				"            throw new InternalError(\"Error in CodeTemplateManager\");// 109\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized int getTemplateCount() {\r\n" + 
				"        return this.templates.size();// 120\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized CodeTemplate[] getTemplates() {\r\n" + 
				"        CodeTemplate[] temp = new CodeTemplate[this.templates.size()];// 130\r\n" + 
				"        return (CodeTemplate[])this.templates.toArray(temp);// 131\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public static final boolean isValidChar(char ch) {\r\n" + 
				"        return RSyntaxUtilities.isLetterOrDigit(ch) || ch == '_';// 143\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized boolean removeTemplate(CodeTemplate template) {\r\n" + 
				"        if (template == null) {// 160\r\n" + 
				"            throw new IllegalArgumentException(\"template cannot be null\");// 161\r\n" + 
				"        } else {\r\n" + 
				"            return this.templates.remove(template);// 165\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized CodeTemplate removeTemplate(String id) {\r\n" + 
				"        if (id == null) {// 182\r\n" + 
				"            throw new IllegalArgumentException(\"id cannot be null\");// 183\r\n" + 
				"        } else {\r\n" + 
				"            Iterator i = this.templates.iterator();// 187\r\n" + 
				"\r\n" + 
				"            CodeTemplate template;\r\n" + 
				"            do {\r\n" + 
				"                if (!i.hasNext()) {\r\n" + 
				"                    return null;// 195\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                template = (CodeTemplate)i.next();// 188\r\n" + 
				"            } while(!id.equals(template.getID()));// 189\r\n" + 
				"\r\n" + 
				"            i.remove();// 190\r\n" + 
				"            return template;// 191\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized void replaceTemplates(CodeTemplate[] newTemplates) {\r\n" + 
				"        this.templates.clear();// 208\r\n" + 
				"        if (newTemplates != null) {// 209\r\n" + 
				"            for(int i = 0; i < newTemplates.length; ++i) {// 210\r\n" + 
				"                this.templates.add(newTemplates[i]);// 211\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        this.sortTemplates();// 214\r\n" + 
				"    }// 215\r\n" + 
				"\r\n" + 
				"    public synchronized boolean saveTemplates() {\r\n" + 
				"        if (this.templates == null) {// 225\r\n" + 
				"            return true;// 226\r\n" + 
				"        } else if (this.directory != null && this.directory.isDirectory()) {// 228\r\n" + 
				"            File[] oldXMLFiles = this.directory.listFiles(new org.fife.ui.rsyntaxtextarea.CodeTemplateManager.XMLFileFilter((1)null));// 234\r\n" + 
				"            if (oldXMLFiles == null) {// 235\r\n" + 
				"                return false;// 236\r\n" + 
				"            } else {\r\n" + 
				"                int count = oldXMLFiles.length;// 238\r\n" + 
				"\r\n" + 
				"                for(int i = 0; i < count; ++i) {// 239\r\n" + 
				"                    oldXMLFiles[i].delete();// 240\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                boolean wasSuccessful = true;// 244\r\n" + 
				"                Iterator var4 = this.templates.iterator();// 245\r\n" + 
				"\r\n" + 
				"                while(var4.hasNext()) {\r\n" + 
				"                    CodeTemplate template = (CodeTemplate)var4.next();\r\n" + 
				"                    File xmlFile = new File(this.directory, template.getID() + \".xml\");// 246\r\n" + 
				"\r\n" + 
				"                    try {\r\n" + 
				"                        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlFile)));// 248\r\n" + 
				"                        e.writeObject(template);// 250\r\n" + 
				"                        e.close();// 251\r\n" + 
				"                    } catch (IOException var8) {// 252\r\n" + 
				"                        var8.printStackTrace();// 253\r\n" + 
				"                        wasSuccessful = false;// 254\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                return wasSuccessful;// 258\r\n" + 
				"            }\r\n" + 
				"        } else {\r\n" + 
				"            return false;// 229\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public synchronized int setTemplateDirectory(File dir) {\r\n" + 
				"        if (dir != null && dir.isDirectory()) {// 274\r\n" + 
				"            this.directory = dir;// 276\r\n" + 
				"            File[] files = dir.listFiles(new org.fife.ui.rsyntaxtextarea.CodeTemplateManager.XMLFileFilter((1)null));// 278\r\n" + 
				"            int newCount = files == null ? 0 : files.length;// 279\r\n" + 
				"            int oldCount = this.templates.size();// 280\r\n" + 
				"            List<CodeTemplate> temp = new ArrayList(oldCount + newCount);// 282\r\n" + 
				"            temp.addAll(this.templates);// 284\r\n" + 
				"\r\n" + 
				"            for(int i = 0; i < newCount; ++i) {// 286\r\n" + 
				"                try {\r\n" + 
				"                    XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(files[i])));// 288\r\n" + 
				"                    Object obj = d.readObject();// 290\r\n" + 
				"                    if (!(obj instanceof CodeTemplate)) {// 291\r\n" + 
				"                        d.close();// 292\r\n" + 
				"                        throw new IOException(\"Not a CodeTemplate: \" + files[i].getAbsolutePath());// 293 294\r\n" + 
				"                    }\r\n" + 
				"\r\n" + 
				"                    temp.add((CodeTemplate)obj);// 296\r\n" + 
				"                    d.close();// 297\r\n" + 
				"                } catch (Exception var9) {// 298\r\n" + 
				"                    var9.printStackTrace();// 302\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            this.templates = temp;// 305\r\n" + 
				"            this.sortTemplates();// 306\r\n" + 
				"            return this.getTemplateCount();// 308\r\n" + 
				"        } else {\r\n" + 
				"            return -1;// 312\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    private synchronized void sortTemplates() {\r\n" + 
				"        this.maxTemplateIDLength = 0;// 325\r\n" + 
				"        Iterator i = this.templates.iterator();// 330\r\n" + 
				"\r\n" + 
				"        while(true) {\r\n" + 
				"            while(i.hasNext()) {\r\n" + 
				"                CodeTemplate temp = (CodeTemplate)i.next();// 331\r\n" + 
				"                if (temp != null && temp.getID() != null) {// 332\r\n" + 
				"                    this.maxTemplateIDLength = Math.max(this.maxTemplateIDLength, temp.getID().length());// 336 337\r\n" + 
				"                } else {\r\n" + 
				"                    i.remove();// 333\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            Collections.sort(this.templates);// 341\r\n" + 
				"            return;// 343\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();

		assertEquals("addTemplate(CodeTemplate) : void", methods.get(0).getName());
		assertEquals("getTemplate(RSyntaxTextArea) : CodeTemplate", methods.get(1).getName());
		assertEquals("getTemplateCount() : int", methods.get(2).getName());
		assertEquals("getTemplates() : CodeTemplate[]", methods.get(3).getName());
		assertEquals("isValidChar(char) : boolean", methods.get(4).getName());
		assertEquals("removeTemplate(CodeTemplate) : boolean", methods.get(5).getName());
		assertEquals("removeTemplate(String) : CodeTemplate", methods.get(6).getName());
		assertEquals("replaceTemplates(CodeTemplate[]) : void", methods.get(7).getName());
		assertEquals("saveTemplates() : boolean", methods.get(8).getName());
		assertEquals("setTemplateDirectory(File) : int", methods.get(9).getName());
		assertEquals("sortTemplates() : void", methods.get(10).getName());
	}

	@Test
	public void testDefaultTokenPainter() {
		String code = "import javax.swing.text.TabExpander;\r\n" + 
				"\r\n" + 
				"class DefaultTokenPainter implements TokenPainter {\r\n" + 
				"    private Float bgRect = new Float();\r\n" + 
				"    private static char[] tabBuf;\r\n" + 
				"\r\n" + 
				"    public final float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e) {\r\n" + 
				"        return this.paint(token, g, x, y, host, e, 0.0F);// 51\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e, float clipStart) {\r\n" + 
				"        return this.paintImpl(token, g, x, y, host, e, clipStart, false, false);// 61\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e, float clipStart, boolean paintBG) {\r\n" + 
				"        return this.paintImpl(token, g, x, y, host, e, clipStart, !paintBG, false);// 72\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    protected void paintBackground(float x, float y, float width, float height, Graphics2D g, int fontAscent, RSyntaxTextArea host, Color color) {\r\n" + 
				"        g.setColor(color);// 92\r\n" + 
				"        this.bgRect.setRect(x, y - (float)fontAscent, width, height);// 93\r\n" + 
				"        g.fillRect((int)x, (int)(y - (float)fontAscent), (int)width, (int)height);// 95\r\n" + 
				"    }// 96\r\n" + 
				"\r\n" + 
				"    protected float paintImpl(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e, float clipStart, boolean selected, boolean useSTC) {\r\n" + 
				"        int origX = (int)x;// 106\r\n" + 
				"        int textOffs = token.getTextOffset();// 107\r\n" + 
				"        char[] text = token.getTextArray();// 108\r\n" + 
				"        int end = textOffs + token.length();// 109\r\n" + 
				"        int flushLen = 0;// 111\r\n" + 
				"        int flushIndex = textOffs;// 112\r\n" + 
				"        Color fg = useSTC ? host.getSelectedTextColor() : host.getForegroundForToken(token);// 113 114\r\n" + 
				"        Color bg = selected ? null : host.getBackgroundForToken(token);// 115\r\n" + 
				"        g.setFont(host.getFontForTokenType(token.getType()));// 116\r\n" + 
				"        FontMetrics fm = host.getFontMetricsForTokenType(token.getType());// 117\r\n" + 
				"\r\n" + 
				"        float nextX;\r\n" + 
				"        for(int i = textOffs; i < end; ++i) {// 119\r\n" + 
				"            switch(text[i]) {// 120\r\n" + 
				"            case '\\t':\r\n" + 
				"                nextX = e.nextTabStop(x + (float)fm.charsWidth(text, flushIndex, flushLen), 0);// 122 123\r\n" + 
				"                if (bg != null) {// 124\r\n" + 
				"                    this.paintBackground(x, y, nextX - x, (float)fm.getHeight(), g, fm.getAscent(), host, bg);// 125 126\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                if (flushLen > 0) {// 128\r\n" + 
				"                    g.setColor(fg);// 129\r\n" + 
				"                    g.drawChars(text, flushIndex, flushLen, (int)x, (int)y);// 130\r\n" + 
				"                    flushLen = 0;// 131\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"                flushIndex = i + 1;// 133\r\n" + 
				"                x = nextX;// 134\r\n" + 
				"                break;// 135\r\n" + 
				"            default:\r\n" + 
				"                ++flushLen;// 137\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        nextX = x + (float)fm.charsWidth(text, flushIndex, flushLen);// 142\r\n" + 
				"        Rectangle r = host.getMatchRectangle();// 143\r\n" + 
				"        if (flushLen > 0 && nextX >= clipStart) {// 145\r\n" + 
				"            if (bg != null) {// 146\r\n" + 
				"                this.paintBackground(x, y, nextX - x, (float)fm.getHeight(), g, fm.getAscent(), host, bg);// 147 148\r\n" + 
				"                if (token.length() == 1 && r != null && (float)r.x == x) {// 149\r\n" + 
				"                    ((RSyntaxTextAreaUI)host.getUI()).paintMatchedBracketImpl(g, host, r);// 150\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            g.setColor(fg);// 154\r\n" + 
				"            g.drawChars(text, flushIndex, flushLen, (int)x, (int)y);// 155\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        if (host.getUnderlineForToken(token)) {// 158\r\n" + 
				"            g.setColor(fg);// 159\r\n" + 
				"            int y2 = (int)(y + 1.0F);// 160\r\n" + 
				"            g.drawLine(origX, y2, (int)nextX, y2);// 161\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        if (host.getPaintTabLines() && origX == host.getMargin().left) {// 167\r\n" + 
				"            this.paintTabLines(token, origX, (int)y, (int)nextX, g, e, host);// 168\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        return nextX;// 171\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public float paintSelected(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e, boolean useSTC) {\r\n" + 
				"        return this.paintSelected(token, g, x, y, host, e, 0.0F, useSTC);// 182\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public float paintSelected(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e, float clipStart, boolean useSTC) {\r\n" + 
				"        return this.paintImpl(token, g, x, y, host, e, clipStart, true, useSTC);// 193\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    protected void paintTabLines(Token token, int x, int y, int endX, Graphics2D g, TabExpander e, RSyntaxTextArea host) {\r\n" + 
				"        if (token.getType() != 21) {// 220\r\n" + 
				"            int offs;\r\n" + 
				"            for(offs = 0; offs < token.length() && RSyntaxUtilities.isWhitespace(token.charAt(offs)); ++offs) {// 221 222 223\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            if (offs < 2) {// 227\r\n" + 
				"                return;// 228\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            endX = (int)token.getWidthUpTo(offs, host, e, 0.0F);// 231\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        FontMetrics fm = host.getFontMetricsForTokenType(token.getType());// 235\r\n" + 
				"        int tabSize = host.getTabSize();// 236\r\n" + 
				"        int tabW;\r\n" + 
				"        if (tabBuf == null || tabBuf.length < tabSize) {// 237\r\n" + 
				"            tabBuf = new char[tabSize];// 238\r\n" + 
				"\r\n" + 
				"            for(tabW = 0; tabW < tabSize; ++tabW) {// 239\r\n" + 
				"                tabBuf[tabW] = ' ';// 240\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        tabW = fm.charsWidth(tabBuf, 0, tabSize);// 248\r\n" + 
				"        g.setColor(host.getTabLineColor());// 252\r\n" + 
				"        int x0 = x + tabW;// 253\r\n" + 
				"        int y0 = y - fm.getAscent();// 254\r\n" + 
				"        if ((y0 & 1) > 0) {// 255\r\n" + 
				"            ++y0;// 257\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        Token next = token.getNextToken();// 261\r\n" + 
				"        if (next == null || !next.isPaintable()) {// 262\r\n" + 
				"            ++endX;// 263\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        while(x0 < endX) {// 265\r\n" + 
				"            int y1 = y0;// 266\r\n" + 
				"\r\n" + 
				"            for(int y2 = y0 + host.getLineHeight(); y1 < y2; y1 += 2) {// 267 268 270\r\n" + 
				"                g.drawLine(x0, y1, x0, y1);// 269\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            x0 += tabW;// 273\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"    }// 276\r\n" + 
				"}\r\n" + 
				"";
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("paint(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander) : float", methods.get(0).getName());
		assertEquals("paint(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander, float) : float", methods.get(1).getName());
		assertEquals("paint(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander, float, boolean) : float", methods.get(2).getName());
		assertEquals("paintBackground(float, float, float, float, Graphics2D, int, RSyntaxTextArea, Color) : void", methods.get(3).getName());
		assertEquals("paintImpl(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander, float, boolean, boolean) : float", methods.get(4).getName());
		assertEquals("paintSelected(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander, boolean) : float", methods.get(5).getName());
		assertEquals("paintSelected(Token, Graphics2D, float, float, RSyntaxTextArea, TabExpander, float, boolean) : float", methods.get(6).getName());
		assertEquals("paintTabLines(Token, int, int, int, Graphics2D, TabExpander, RSyntaxTextArea) : void", methods.get(7).getName());

	}

	@Test
	public void testTokenUtils() {
		String code = "import javax.swing.text.TabExpander;\r\n" + 
				"\r\n" + 
				"public final class TokenUtils {\r\n" + 
				"    private TokenUtils() {\r\n" + 
				"    }// 15\r\n" + 
				"\r\n" + 
				"    public static org.fife.ui.rsyntaxtextarea.TokenUtils.TokenSubList getSubTokenList(Token tokenList, int pos, TabExpander e, RSyntaxTextArea textArea, float x0) {\r\n" + 
				"        return getSubTokenList(tokenList, pos, e, textArea, x0, (TokenImpl)null);// 58\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public static org.fife.ui.rsyntaxtextarea.TokenUtils.TokenSubList getSubTokenList(Token tokenList, int pos, TabExpander e, RSyntaxTextArea textArea, float x0, TokenImpl tempToken) {\r\n" + 
				"        if (tempToken == null) {// 107\r\n" + 
				"            tempToken = new TokenImpl();// 108\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        Token t;\r\n" + 
				"        for(t = tokenList; t != null && t.isPaintable() && !t.containsPosition(pos); t = t.getNextToken()) {// 110 114 116\r\n" + 
				"            x0 += t.getWidth(textArea, e, x0);// 115\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"        if (t != null && t.isPaintable()) {// 120\r\n" + 
				"            if (t.getOffset() != pos) {// 122\r\n" + 
				"                int difference = pos - t.getOffset();// 124\r\n" + 
				"                x0 += t.getWidthUpTo(t.length() - difference + 1, textArea, e, x0);// 125\r\n" + 
				"                tempToken.copyFrom(t);// 126\r\n" + 
				"                tempToken.makeStartAt(pos);// 127\r\n" + 
				"                return new org.fife.ui.rsyntaxtextarea.TokenUtils.TokenSubList(tempToken, x0);// 129\r\n" + 
				"            } else {\r\n" + 
				"                return new org.fife.ui.rsyntaxtextarea.TokenUtils.TokenSubList(t, x0);// 133\r\n" + 
				"            }\r\n" + 
				"        } else {\r\n" + 
				"            return new org.fife.ui.rsyntaxtextarea.TokenUtils.TokenSubList(tokenList, x0);// 139\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();

		assertEquals("TokenUtils()", methods.get(0).getName());
		assertEquals("getSubTokenList(Token, int, TabExpander, RSyntaxTextArea, float) : TokenSubList", methods.get(1).getName());
		assertEquals("getSubTokenList(Token, int, TabExpander, RSyntaxTextArea, float, TokenImpl) : TokenSubList", methods.get(2).getName());

		assertEquals(JavaMethod.ACCESS.PUBLIC, ((JavaMethod)methods.get(1)).access);
		assertEquals(JavaMethod.ACCESS.PUBLIC, ((JavaMethod)methods.get(2)).access);
	}

}

