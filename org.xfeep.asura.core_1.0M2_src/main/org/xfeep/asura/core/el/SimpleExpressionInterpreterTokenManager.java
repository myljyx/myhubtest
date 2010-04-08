/* Generated By:JavaCC: Do not edit this line. SimpleExpressionInterpreterTokenManager.java */
/*******************************************************************************
 * Copyright (c) 2008-2009 zhang yuexiang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xfeep.asura.core.el;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.xfeep.asura.core.reflect.TypeInfoPool;

/** Token Manager. */
@SuppressWarnings("all")
public class SimpleExpressionInterpreterTokenManager implements SimpleExpressionInterpreterConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x1c000L) != 0L)
         {
            jjmatchedKind = 21;
            return 24;
         }
         return -1;
      case 1:
         if ((active0 & 0x1c000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 1;
            return 24;
         }
         return -1;
      case 2:
         if ((active0 & 0x1c000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 2;
            return 24;
         }
         return -1;
      case 3:
         if ((active0 & 0x14000L) != 0L)
            return 24;
         if ((active0 & 0x8000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 3;
            return 24;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 5;
         return jjMoveStringLiteralDfa1_0(0x200L);
      case 38:
         return jjStopAtPos(0, 6);
      case 60:
         jjmatchedKind = 11;
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 61:
         return jjStopAtPos(0, 8);
      case 62:
         jjmatchedKind = 10;
         return jjMoveStringLiteralDfa1_0(0x1000L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x10000L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 124:
         return jjStopAtPos(0, 7);
      default :
         return jjMoveNfa_0(1, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x200L) != 0L)
            return jjStopAtPos(1, 9);
         else if ((active0 & 0x1000L) != 0L)
            return jjStopAtPos(1, 12);
         else if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(1, 13);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x10000L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x18000L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(3, 14, 24);
         break;
      case 108:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(3, 16, 24);
         break;
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(4, 15, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x1ff00000fffffffeL, 0xffffffffffffc000L, 0xffffffffL, 0x600000000000000L
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec5 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec6 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L
};
static final long[] jjbitVec7 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x3fffffffffffL, 0x0L, 0x0L, 0x0L
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 24;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 24:
                  if ((0x3ff001000000000L & l) != 0L)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  else if (curChar == 46)
                     jjstateSet[jjnewStateCnt++] = 22;
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 17)
                        kind = 17;
                     jjCheckNAdd(0);
                  }
                  else if (curChar == 36)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 39)
                     jjCheckNAddStates(3, 5);
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAdd(0);
                  break;
               case 2:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 4:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 5:
                  if (curChar == 39 && kind > 18)
                     kind = 18;
                  break;
               case 6:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(6, 9);
                  break;
               case 7:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 8:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 9:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(7);
                  break;
               case 10:
                  if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  break;
               case 11:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 13:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 14:
                  if (curChar == 34 && kind > 18)
                     kind = 18;
                  break;
               case 15:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(10, 13);
                  break;
               case 16:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 17:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 18:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(16);
                  break;
               case 19:
                  if (curChar != 36)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 20:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 21:
                  if (curChar == 46)
                     jjstateSet[jjnewStateCnt++] = 22;
                  break;
               case 22:
                  if (curChar != 36)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(21, 23);
                  break;
               case 23:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(21, 23);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 24:
               case 20:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 1:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 2:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 3:
                  if (curChar == 92)
                     jjAddStates(14, 16);
                  break;
               case 4:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 11:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 12:
                  if (curChar == 92)
                     jjAddStates(17, 19);
                  break;
               case 13:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 22:
               case 23:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(21, 23);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 24:
               case 20:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 1:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 2:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(3, 5);
                  break;
               case 11:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(0, 2);
                  break;
               case 22:
               case 23:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddTwoStates(21, 23);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 24 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   11, 12, 14, 2, 3, 5, 2, 3, 7, 5, 11, 12, 16, 14, 4, 6, 
   8, 13, 15, 17, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec8[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, "\41", "\46", "\174", "\75", "\41\75", "\76", 
"\74", "\76\75", "\74\75", "\164\162\165\145", "\146\141\154\163\145", 
"\156\165\154\154", null, null, null, null, null, null, null, };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x27ffe1L, 
};
static final long[] jjtoSkip = {
   0x1eL, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[24];
private final int[] jjstateSet = new int[48];
protected char curChar;
/** Constructor. */
public SimpleExpressionInterpreterTokenManager(JavaCharStream stream){
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public SimpleExpressionInterpreterTokenManager(JavaCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 24; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
