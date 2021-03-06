/**  \file parser.hpp \brief Functions to parse strings */
/*
-----------------------------------------------------------------------------
   Copyright (C) 2011-2013 Ruben Martinez-Cantin <rmcantin@unizar.es>
 
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
-----------------------------------------------------------------------------
*/
#ifndef  _PARSER_HPP_
#define  _PARSER_HPP_

#include <vector>

namespace bayesopt 
{  
  namespace utils 
  {
    /**
     * Parse expresions of the form Parent(Child1, Child2). The "childs"
     * can also be expressions of the same type.
     */
    int parseExpresion(std::string input, std::string& parent,
		       std::string& child1, std::string& child2);

    /**
     * Parse expresions of the form Parent(Child1, ... ,ChildN). The "childs"
     * can also be expressions of the same type.
     */
    int parseExpresion(std::string input, std::string& parent,
		       std::vector<std::string>& childs);

  
  } //namespace utils

} //namespace bayesopt

#endif
