/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.mina.core.future;

import org.apache.mina.core.future.IoFuture;

public interface UnbindFuture extends IoFuture {

    /**
     * Reports immediately whether the unbind operation has successfully completed
     * @return true if the unbind operation has successfully completed, else false
     */
    boolean isUnbound();

    /**
     * Signal that the unbind operation has successfully completed (fulfills the future)
     */
    void setUnbound();

    /**
     * Immediately gets any exception thrown by the unbind processing (if completed)
     * @return the exception, or null if the unbind operation has not yet completed or completed successfully
     */
    Throwable getException();

    /**
     * Signal that the unbind operation has failed (fulfills the future)
     * @param exception
     */
    void setException(Throwable exception);
}
