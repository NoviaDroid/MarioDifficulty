/**  \file bayesoptwpr.h \brief C-wrapper for Bayesian optimization */
/*
-----------------------------------------------------------------------------
   This file is part of BayesOptimization, an efficient C++ library for 
   Bayesian optimization.

   Copyright (C) 2011-2013 Ruben Martinez-Cantin <rmcantin@unizar.es>
 
   BayesOptimization is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   BayesOptimization is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with BayesOptimization.  If not, see <http://www.gnu.org/licenses/>.
-----------------------------------------------------------------------------
*/

#ifndef _BAYESOPTWPR_H_
#define _BAYESOPTWPR_H_

#include "parameters.h"

/** \addtogroup BayesOpt */
/*@{*/

#ifdef __cplusplus
extern "C" {
#endif 

  /* TODO: make it const double *x */
  typedef double (*eval_func)(unsigned int n, const double *x,
			      double *gradient, /* NULL if not needed */
			      void *func_data);


/** 
 * @brief C functional wrapper for the Bayesian optimization algorithm. 
 * This is an efficient, C/C++ implementation of the Bayesian optimization.
 * Basically, it uses the active learning strategy to optimize an "arbitrary" 
 * funtion using few iterations. This assumes continuous optimization.
 * 
 * @param nDim number of input dimensions
 * @param f pointer to the function to optimize
 * @param f_data pointer to extra data to be used by f
 * @param lb array of lower bounds
 * @param ub array of upper bounds
 * @param x input: initial query, output: result (minimum)
 * @param minf value of the function at the minimum
 * @param parameters parameters for the Bayesian optimization.
 * 
 * @return error code
 */
  BAYESOPT_API int bayes_optimization(int nDim, eval_func f, void* f_data,
			 const double *lb, const double *ub,
			 double *x, double *minf,
			 bopt_params parameters);


/** 
 * @brief C functional wrapper for the Bayesian optimization algorithm. 
 * This is an efficient, C/C++ implementation of the Bayesian optimization.
 * Basically, it uses the active learning strategy to optimize an "arbitrary" 
 * funtion using few iterations. This assumes the discrete optimization.
 * 
 * @param nDim number of input dimensions
 * @param f pointer to the function to optimize
 * @param f_data pointer to extra data to be used by f
 * @param valid_x set of possible discrete points
 * @param n_points number of possible discrete points
 * @param x input: initial query, output: result (minimum)
 * @param minf value of the function at the minimum
 * @param parameters parameters for the Bayesian optimization.
 * 
 * @return error code
 */
  BAYESOPT_API int bayes_optimization_disc(int nDim, eval_func f, void* f_data,
			      double *valid_x, size_t n_points,
			      double *x, double *minf,
			      bopt_params parameters);

  
#ifdef __cplusplus
}
#endif 

/**@}*/

#endif
