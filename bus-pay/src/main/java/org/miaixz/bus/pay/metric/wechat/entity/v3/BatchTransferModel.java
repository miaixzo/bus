/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Model for the Initiate Batch Transfer API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransferModel {

    /**
     * The merchant's internal batch number. It must be unique within the merchant's system and can only consist of
     * numbers and uppercase/lowercase letters.
     */
    private String out_batch_no;

    /**
     * The name of this batch transfer.
     */
    private String batch_name;
    /**
     * A description for the transfer, UTF-8 encoded, up to 32 characters.
     */
    private String batch_remark;
    /**
     * The total transfer amount in cents. This must be equal to the sum of all individual transfer amounts in the
     * batch.
     */
    private Integer total_amount;
    /**
     * The total number of transfers in this batch, up to 1000. This must match the number of items in the transfer
     * detail list.
     */
    private Integer total_num;
    /**
     * The list of individual transfer details for this batch, up to 1000 items.
     */
    private List<TransferDetailInput> transfer_detail_list;

    /**
     * The scene ID specifying the use case for this transfer.
     */
    private String transfer_scene_id;

}
