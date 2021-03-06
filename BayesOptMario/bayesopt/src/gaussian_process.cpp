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

#include "cholesky.hpp"
#include "trace_ublas.hpp"
#include "gaussian_process.hpp"

namespace bayesopt
{

  namespace ublas = boost::numeric::ublas;

  GaussianProcess::GaussianProcess(size_t dim, bopt_params params):
    NonParametricProcess(dim, params)
  {
    d_ = new GaussianDistribution();
  }  // Constructor


  GaussianProcess::~GaussianProcess()
  {
    delete d_;
  } // Default destructor


  double GaussianProcess::negativeTotalLogLikelihood()
  {
    // In this case it is equivalent.
    return negativeLogLikelihood();
  }


  double GaussianProcess::negativeLogLikelihood()
  {
    const matrixd K = computeCorrMatrix();
    const size_t n = K.size1();
    matrixd L(n,n);
    utils::cholesky_decompose(K,L);

    vectord alpha(mGPY-prod(mMu,mFeatM));
    inplace_solve(L,alpha,ublas::lower_tag());
    double loglik = ublas::inner_prod(alpha,alpha)/(2*mSigma) + 
      utils::log_trace(L);
    return loglik;
  }

  ProbabilityDistribution* GaussianProcess::prediction(const vectord &query)
  {
    const double kq = (*mKernel)(query, query);
    const vectord kn = computeCrossCorrelation(query);
    

    vectord vd(kn);
    inplace_solve(mL,vd,ublas::lower_tag());
    double basisPred = ublas::inner_prod(mMu,mMean->getFeatures(query));
    double yPred = basisPred + ublas::inner_prod(vd,mAlphaV);
    double sPred = sqrt(mSigma*(kq - ublas::inner_prod(vd,vd)));
    
    d_->setMeanAndStd(yPred,sPred);
    return d_;
  }


  int GaussianProcess::precomputePrediction()
  {
    const size_t n = mGPY.size();
  
    mAlphaV.resize(n,false);
    mAlphaV = mGPY-prod(mMu,mFeatM);
    inplace_solve(mL,mAlphaV,ublas::lower_tag());

    return 0; 
  }
	
} //namespace bayesopt
