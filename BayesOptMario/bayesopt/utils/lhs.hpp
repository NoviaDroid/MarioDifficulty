/**  \file lhs.hpp \brief Latin Hypercube Sampling. */
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

#ifndef _LHS_HPP_
#define _LHS_HPP_

#include "randgen.hpp"
#include "indexvector.hpp"
#if defined (USE_SOBOL)
#  include "sobol.hpp"
#endif

namespace bayesopt
{
  namespace utils
  {      
    
    /** \brief Modify an array using ramdom permutations.
     *
     * It is used to generate a uniform Latin hypercube.
     * Equivalent to std::random_shuffle but using boost::random
     */
    template<class D>
    void randomPerms(D& arr, 
		     randEngine& mtRandom)
    {
      typedef typename D::iterator iter;

      randInt sample(mtRandom, intUniformDist(0,arr.size()-1));
      for (iter it=arr.begin(); it!=arr.end(); ++it)
	iter_swap(arr.begin()+sample(),it);
    } // randomPerms 


    /** \brief Latin hypercube sampling
     * It is used to generate a uniform Latin hypercube
     */
    template<class M>
    int lhs(M& Result,
	    randEngine& mtRandom)
    {
      randFloat sample( mtRandom, realUniformDist(0,1) );
      size_t nA = Result.size1();
      size_t nB = Result.size2();
      double ndA = static_cast<double>(nA);
      //  std::vector<int> perms(nA);
  
      for (size_t i = 0; i < nB; i++)
	{
	  std::vector<int> perms = returnIndexVector(nA);
	  randomPerms(perms, mtRandom);
	  //std::random_shuffle(perms.begin(),perms.end());
      
	  for (size_t j = 0; j < nA; j++)
	    {		
	      Result(j,i) = ( static_cast<double>(perms[j]) - sample() ) / ndA;
	    }
	}

      return 0;
    }

    /** \brief Hypercube sampling based on Sobol sequences
     * It uses the external Sobol library. Thus it do not depend on
     * boost random.
     */
#if defined (USE_SOBOL)
    template<class M>
    int sobol(M& result, long long int seed)
    {
      size_t nSamples = result.size1();
      size_t nDims = result.size2();

      double *sobol_seq = i8_sobol_generate(nDims,nSamples,seed);
      
      for(size_t ii = 0; ii<nSamples; ++ii)
	{
	  for(size_t jj=0; jj<nDims; ++jj)
	    {
	      result(ii,jj) = *sobol_seq++;
	    }
	}
      return 0;
    }
#endif

    /** \brief Uniform hypercube sampling
     * It is used to generate a set of uniformly distributed
     * samples in hypercube
     */
    template<class M>
    int uniformSampling(M& Result,
			randEngine& mtRandom)
    {
      randFloat sample( mtRandom, realUniformDist(0,1) );
      size_t nA = Result.size1();
      size_t nB = Result.size2();

      // TODO: Improve with iterators
      for (size_t i = 0; i < nA; i++)
	for (size_t j = 0; j < nB; j++)
	  Result(i,j) = sample();

      return 0;
    }

    /** \brief Selects the sampling method.  */
    template<class M>
    int samplePoints(M& xPoints, int method)
    {
      randEngine mtRandom;

      if (method == 1) 
	lhs(xPoints, mtRandom);
      else 
	if (method == 2)
	  {
#if defined (USE_SOBOL)
	    sobol(xPoints, 0);
#else
	    lhs(xPoints, mtRandom);
#endif
	  }
	else
	  uniformSampling(xPoints, mtRandom);

      return 0;
    }


  } //namespace utils

} //namespace bayesopt

#endif
