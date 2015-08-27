/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam;

import processing.core.PMatrix3D;

/**
 *
 * @author jiii
 */
public interface HasExtrinsics {
    
    public boolean hasExtrinsics();
    public PMatrix3D getExtrinsics();
    
}
