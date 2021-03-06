/*
-------------------------------------------------------------------------
   This file is part of BayesOpt, an efficient C++ library for 
   Bayesian optimization.

   Copyright (C) 2011-2013 Ruben Martinez-Cantin <rmcantin@unizar.es>
 
   BayesOpt is free software: you can redistribute it and/or modify it 
   under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   BayesOpt is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with BayesOpt.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------
*/

// BOOST Libraries
#include <boost/numeric/ublas/matrix.hpp>
#include <boost/numeric/ublas/matrix_proxy.hpp>
#include <boost/numeric/ublas/vector.hpp>

#include "nloptwpr.h"
#include "inneroptimization.hpp"

namespace NLOPT_WPR
{

  namespace ublas = boost::numeric::ublas;
  using bayesopt::InnerOptimization;


  double evaluate_nlopt (unsigned int n, const double *x,
			 double *grad, void *my_func_data)

  {
    double xcopy[128];
    for (unsigned int i=0;i<n;i++)
      xcopy[i] = x[i];
    ublas::array_adaptor<double> shared(n, xcopy);
    ublas::vector<double, ublas::array_adaptor<double> > sharedN(n, shared); 
    
    // This is not very clever... but works!
    void *objPointer = my_func_data;
    InnerOptimization* OPTIMIZER = static_cast<InnerOptimization*>(objPointer);
    
    return OPTIMIZER->innerEvaluate(sharedN);
  } /* evaluate_criteria_nlopt */


  double evaluate_nlopt_grad (unsigned int n, const double *x,
			      double *grad, void *my_func_data)

  {
    double xcopy[128];
    for (unsigned int i=0;i<n;i++)
      xcopy[i] = x[i];
    ublas::array_adaptor<double> shared(n, xcopy);
    ublas::vector<double, ublas::array_adaptor<double> > sharedN(n, shared); 
    
    // This is not very clever... but works!
    void *objPointer = my_func_data;
    InnerOptimization* OPTIMIZER = static_cast<InnerOptimization*>(objPointer);
    
    ublas::vector<double> vgrad = ublas::zero_vector<double>(n);

    double f =  OPTIMIZER->innerEvaluate(sharedN,vgrad);
  
    if ((grad) && (grad != NULL) )
      for (unsigned int i=0;i<n;i++)
	grad[i] = vgrad(i);


    return f;
  } /* evaluate_criteria_nlopt */
}
