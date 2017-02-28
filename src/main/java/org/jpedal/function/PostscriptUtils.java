/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * PostscriptUtils.java
 * ---------------
 */

package org.jpedal.function;

import static org.jpedal.function.PostscriptFactory.*;

/**
 *
 * @author markee
 */
class PostscriptUtils {

    
	//get string for command
	static String toString(final int id){

		final String str;

		switch(id){
		case PS_abs:
			str="abs";
			break;
		case PS_add:
			str="add";
			break;
		case PS_atan:
			str="atan";
			break;
		case PS_ceil:
			str="ceiling";
			break;
		case PS_cos:
			str="cos";
			break;
		case PS_cvi:
			str="cvi";
			break;
		case PS_cvr:
			str="cvr";
			break;
		case PS_div:
			str="div";
			break;
		case PS_exp:
			str="exp";
			break;
		case PS_floo:
			str="floor";
			break;
		case PS_idiv:
			str="idiv";
			break;
		case PS_ln:
			str="ln";
			break;
		case PS_log:
			str="log";
			break;
		case PS_mod:
			str="mod";
			break;
		case PS_mul:
			str="mul";
			break;
		case PS_neg:
			str="neg";
			break;
		case PS_sin:
			str="sin";
			break;
		case PS_sqrt:
			str="sqrt";
			break;
		case PS_sub:
			str="sub";
			break;
		case PS_roun:
			str="round";
			break;
		case PS_trun:
			str="truncate";
			break;
		case PS_and:
			str="and";
			break;
		case PS_bits:
			str="bitshift";
			break;
		case PS_eq:
			str="eq";
			break;
		case PS_fals:
			str="false";
			break;
		case PS_ge:
			str="ge";
			break;
		case PS_gt:
			str="gt";
			break;
		case PS_le:
			str="le";
			break;
		case PS_lt:
			str="lt";
			break;
		case PS_ne:
			str="ne";
			break;
		case PS_not:
			str="not";
			break;
		case PS_or:
			str="or";
			break;
		case PS_true:
			str="true";
			break;
		case PS_xor:
			str="xor";
			break;
		case PS_if:
			str="if";
			break;
		case PS_ifel:
			str="ifelse";
			break;
		case PS_copy:
			str="copy";
			break;
		case PS_exch:
			str="exch";
			break;
		case PS_pop:
			str="pop";
			break;
		case PS_dup:
			str="dup";
			break;
		case PS_inde:
			str="index";
			break;
		case PS_roll:
			str="roll";
			break;
		default:
			str="UNKNOWN";

		}
		return str;

	}

}
