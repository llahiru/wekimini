package wekimini.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.LearningManager;
import wekimini.Path;
import wekimini.StatusUpdateCenter;
import wekimini.TrainingRunner;
import wekimini.Wekinator;
import wekimini.osc.OSCMonitor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fiebrink
 */
public class LearningPanel extends javax.swing.JPanel {

    private Wekinator w;
    private final ImageIcon onIcon = new ImageIcon(getClass().getResource("/wekimini/icons/green3.png")); // NOI18N
    private final ImageIcon offIcon = new ImageIcon(getClass().getResource("/wekimini/icons/yellow2.png")); // NOI18N
    private final ImageIcon problemIcon = new ImageIcon(getClass().getResource("/wekimini/icons/redx1.png")); // NOI18N
    private final ImageIcon problemIcon2 = new ImageIcon(getClass().getResource("/wekimini/icons/red1.png")); // NOI18N

    
    
    int lastRoundAdvertised = 0;

    /**
     * Creates new form TestLearningPanel1
     */
    public LearningPanel() {
        initComponents();
    }

    public void setup(Wekinator w, Path[] ps, String[] modelNames) {
        this.w = w;

        simpleLearningSet1.setup(w, ps, modelNames);
       // w.getLearningManager().addPropertyChangeListener(this::learningManagerPropertyChanged);
        w.getLearningManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        });
        

        w.getTrainingRunner().addCancelledListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                learningCancelled();
            }
        });

        w.getTrainingRunner().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == TrainingRunner.PROP_TRAININGPROGRESS) {
                    trainerUpdated((TrainingRunner.TrainingStatus) evt.getNewValue());
                }

            }
        });

       // w.getStatusUpdateCenter().addPropertyChangeListener(this::statusUpdated);
        w.getStatusUpdateCenter().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                statusUpdated(evt);
            }
        });
        
        if (w.getStatusUpdateCenter().getLastUpdate() == null) {
            setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
        } else {
            setStatus(w.getStatusUpdateCenter().getLastUpdate().toString());
        }

        w.getOSCMonitor().startMonitoring();
        w.getOSCMonitor().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscMonitorChanged(evt);
            }
        });
        setButtonsForLearningState();
        updateDeleteLastRoundButton();
        setInIcon(w.getOSCMonitor().getReceiveState());
        setOutIcon(w.getOSCMonitor().isSending());

    }

    private void setInIcon(OSCMonitor.OSCReceiveState rstate) {
        if (rstate == OSCMonitor.OSCReceiveState.NOT_CONNECTED) {
            indicatorOscIn.setIcon(problemIcon);
            indicatorOscIn.setToolTipText("OSC receiver is not listening");
            // System.out.println("OSC recv not connected");
        } else if (rstate == OSCMonitor.OSCReceiveState.CONNECTED_NODATA) {
            indicatorOscIn.setIcon(offIcon);
            indicatorOscIn.setToolTipText("Listening, but no data arriving");

            System.out.println("Connected no data");
        } else if (rstate == OSCMonitor.OSCReceiveState.RECEIVING) {
            indicatorOscIn.setIcon(onIcon);
            indicatorOscIn.setToolTipText("Receiving inputs");

            //System.out.println("Receiving");
        } else {
            //There's a problem!
            indicatorOscIn.setIcon(problemIcon2);
            indicatorOscIn.setToolTipText("Wrong number of inputs received");
        }
    }

    private void setOutIcon(boolean isSending) {
        if (isSending) {
            indicatorOscOut.setIcon(onIcon);
            indicatorOscOut.setToolTipText("Sending outputs");

            System.out.println("SENDING");
        } else {
            indicatorOscOut.setIcon(offIcon);
            indicatorOscOut.setToolTipText("Not sending outputs");

            System.out.println("NOT SENDING");
        }
    }

    private void oscMonitorChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCMonitor.PROP_RECEIVE_STATE) {
            setInIcon((OSCMonitor.OSCReceiveState) evt.getNewValue());

        } else if (evt.getPropertyName() == OSCMonitor.PROP_ISSENDING) {
            setOutIcon((Boolean) evt.getNewValue());
        }
    }

    private void statusUpdated(PropertyChangeEvent evt) {
        StatusUpdateCenter.StatusUpdate u = (StatusUpdateCenter.StatusUpdate) evt.getNewValue();
        setStatus(u.toString());
    }

    private void learningCancelled() {
        //setStatus("Training was cancelled.");
        w.getStatusUpdateCenter().update(this, "Training was cancelled");

    }

    private void trainerUpdated(TrainingRunner.TrainingStatus newStatus) {
        String s = newStatus.getNumTrained() + " of " + newStatus.getNumToTrain()
                + " models trained, " + newStatus.getNumErrorsEncountered()
                + " errors encountered.";
        if (newStatus.isWasCancelled()) {
            s += " Training cancelled.";
        }
      //  setStatus(s);
        w.getStatusUpdateCenter().update(this, s);
    }

    private void updateDeleteLastRoundButton() {
        int lastRound = w.getLearningManager().getRecordingRound();
        int numLastRound = w.getDataManager().getNumExamplesInRound(lastRound);
        //Look for most recent round with >0 examples recorded.
        while (lastRound > 0 && numLastRound == 0) {
            lastRound--;
            numLastRound = w.getDataManager().getNumExamplesInRound(lastRound);
        }
        if (numLastRound > 0) {
            lastRoundAdvertised = lastRound;
            buttonDeleteLastRecording.setText("Delete last recording (#"
                    + lastRoundAdvertised + ")");
            buttonDeleteLastRecording.setEnabled(true);
        } else {
            buttonDeleteLastRecording.setText("Delete last recording");
            buttonDeleteLastRecording.setEnabled(false);
        }
    }

    private void updateForRecordingState(LearningManager.RecordingState rs) {
        updateRecordingButton();
        updateButtonStates();
        if (rs == LearningManager.RecordingState.NOT_RECORDING) {
            updateDeleteLastRoundButton();
        }
    }

    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == LearningManager.PROP_RECORDINGSTATE) {
            updateForRecordingState((LearningManager.RecordingState) evt.getNewValue());
        } else if (evt.getPropertyName() == LearningManager.PROP_LEARNINGSTATE) {
            setButtonsForLearningState();
            updateStatusForLearningState();
            // System.out.println("Learning state updated: " + w.getLearningManager().getLearningState());
        } else if (evt.getPropertyName() == LearningManager.PROP_RUNNINGSTATE) {
            updateRunButtonAndText();
        } else if (evt.getPropertyName() == LearningManager.PROP_NUMEXAMPLESTHISROUND) {
            //TODO: Update somewhere else
            w.getStatusUpdateCenter().update(this, w.getLearningManager().getNumExamplesThisRound() + " new examples recorded");
            //setStatus();
        } else if (evt.getPropertyName() == LearningManager.PROP_ABLE_TO_RECORD) {
            setButtonsForLearningState();
        } else if (evt.getPropertyName() == LearningManager.PROP_ABLE_TO_RUN) {
            setButtonsForLearningState();
        }
//Could do training state for GUI too... Want to have singleton Training Worker so all GUI elements can access update info
    }

    private void updateRunButtonAndText() {
        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING) {
            buttonRun.setText("Stop running");
            buttonRun.setForeground(Color.RED);
          //  setStatus("Running.");
            w.getStatusUpdateCenter().update(this, "Running");
        } else {
            buttonRun.setText("Run");
            buttonRun.setForeground(Color.BLACK);

           // setStatus("Stopped running.");
            w.getStatusUpdateCenter().update(this, "Stopped running");

        }
    }

    private void updateStatusForTrainingWorker() {

    }

    private void updateStatusForLearningState() {
        LearningManager.LearningState ls = w.getLearningManager().getLearningState();
        if (ls == LearningManager.LearningState.NOT_READY_TO_TRAIN) {
           // setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
            w.getStatusUpdateCenter().update(this, "Ready to go! Press \"Start Recording\" above to record some examples.");

        } else if (ls == LearningManager.LearningState.TRAINING) {
            //setStatus("Training...");
            w.getStatusUpdateCenter().update(this, "Training...");

        } else if (ls == LearningManager.LearningState.DONE_TRAINING) {
            if (w.getTrainingRunner().wasCancelled()) {
                 w.getStatusUpdateCenter().update(this, "Training was cancelled.");
            } else if (w.getTrainingRunner().errorEncountered()) {
                 w.getStatusUpdateCenter().update(this, "Error(s) encountered during training.");
            } else {
                int n = w.getLearningManager().numRunnableModels();
                if (n > 0) {
                     w.getStatusUpdateCenter().update(this, "Training completed. Press \"Run\" to run trained models.");
                } else {
                     w.getStatusUpdateCenter().update(this, "No models are ready to run. Record data and/or train.");
                }
            }
        } else if (ls == LearningManager.LearningState.READY_TO_TRAIN) {
            w.getStatusUpdateCenter().update(this, w.getLearningManager().getNumExamplesThisRound() + " new examples recorded");
           // setStatus("Examples recorded. Press \"Train\" to build models from data.");
        }
    }

    private void setButtonsForLearningState() {
        buttonRun.setEnabled(w.getLearningManager().isAbleToRun());
        buttonRecord.setEnabled(w.getLearningManager().isAbleToRecord());

        LearningManager.LearningState ls = w.getLearningManager().getLearningState();
        if (ls == LearningManager.LearningState.NOT_READY_TO_TRAIN) {
            buttonTrain.setText("Train");
            buttonTrain.setEnabled(false);
            buttonTrain.setForeground(Color.BLACK);
        } else if (ls == LearningManager.LearningState.TRAINING) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Cancel training");
            buttonTrain.setForeground(Color.RED);
        } else if (ls == LearningManager.LearningState.DONE_TRAINING) {
            buttonTrain.setEnabled(true); //Don't prevent immediate retraining; some model builders may give different models on same data.
            buttonTrain.setText("Train");
            buttonTrain.setForeground(Color.BLACK);
        } else if (ls == LearningManager.LearningState.READY_TO_TRAIN) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Train");
            buttonTrain.setForeground(Color.BLACK);
        }
    }

    private void updateRecordingButton() {
        if (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING) {
            buttonRecord.setText("Stop Recording");
            buttonRecord.setForeground(Color.red);
        } else {
            buttonRecord.setText("Start Recording");
            buttonRecord.setForeground(Color.black);
        }
    }

    //TODO: Remove many listeners for this and replace them with update from Status Center
    private void setStatus(String s) {
        labelStatus.setText(s);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        buttonRecord = new javax.swing.JButton();
        buttonTrain = new javax.swing.JButton();
        buttonRun = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonDeleteLastRecording = new javax.swing.JButton();
        buttonReAddLastRecording = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        indicatorOscIn = new javax.swing.JLabel();
        indicatorOscOut = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        simpleLearningSet1 = new wekimini.gui.SimpleLearningSet();
        panelStatus = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        buttonRecord.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonRecord.setText("Start Recording");
        buttonRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRecordActionPerformed(evt);
            }
        });

        buttonTrain.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonTrain.setText("<html>Train</html>");
        buttonTrain.setEnabled(false);
        buttonTrain.setMaximumSize(new java.awt.Dimension(75, 29));
        buttonTrain.setPreferredSize(new java.awt.Dimension(132, 29));
        buttonTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTrainActionPerformed(evt);
            }
        });

        buttonRun.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonRun.setText("Run");
        buttonRun.setEnabled(false);
        buttonRun.setPreferredSize(new java.awt.Dimension(132, 29));
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });

        buttonDeleteLastRecording.setEnabled(false);
        buttonDeleteLastRecording.setLabel("Delete last recording ");
        buttonDeleteLastRecording.setMaximumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.setMinimumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.setPreferredSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteLastRecordingActionPerformed(evt);
            }
        });

        buttonReAddLastRecording.setEnabled(false);
        buttonReAddLastRecording.setLabel("Re-add last recording");
        buttonReAddLastRecording.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReAddLastRecordingActionPerformed(evt);
            }
        });

        indicatorOscIn.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        indicatorOscIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/green3.png"))); // NOI18N
        indicatorOscIn.setText("OSC In");
        indicatorOscIn.setToolTipText("Receiver is not listening");
        indicatorOscIn.setPreferredSize(new java.awt.Dimension(45, 28));
        indicatorOscIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                indicatorOscInMouseClicked(evt);
            }
        });

        indicatorOscOut.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        indicatorOscOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/yellow2.png"))); // NOI18N
        indicatorOscOut.setText("OSC Out");
        indicatorOscOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                indicatorOscOutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonDeleteLastRecording, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonReAddLastRecording, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(buttonRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonTrain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(indicatorOscIn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(indicatorOscOut)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(indicatorOscIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indicatorOscOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonTrain, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonDeleteLastRecording, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonReAddLastRecording))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(simpleLearningSet1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
            .addComponent(simpleLearningSet1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
        );

        panelStatus.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Status:");

        labelStatus.setText(" ");

        javax.swing.GroupLayout panelStatusLayout = new javax.swing.GroupLayout(panelStatus);
        panelStatus.setLayout(panelStatusLayout);
        panelStatusLayout.setHorizontalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelStatusLayout.setVerticalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonStates() {
        // if ()

        /*if (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING) {
         buttonRecord.setEnabled(true);
         buttonTrain.setEnabled(false);
         buttonRun.setEnabled(false);
         } else if (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING) {
         buttonRecord.setEnabled(true);
         buttonTrain.setEnabled(false);
         buttonRun.setEnabled(false);
         } */
    }

    private void buttonRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRecordActionPerformed

        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING) {
            w.getLearningManager().setRunningState(LearningManager.RunningState.NOT_RUNNING);
        }

        if (w.getLearningManager().getRecordingState() != LearningManager.RecordingState.RECORDING) {
            w.getLearningManager().startRecording();
             w.getStatusUpdateCenter().update(this, "Recording - waiting for inputs to arrive");
        } else {
            w.getLearningManager().stopRecording();
           // setStatus("Examples recorded. Press \"Train\" to build models from data.");
            w.getStatusUpdateCenter().update(this, w.getLearningManager().getNumExamplesThisRound() + " new examples recorded");

        }
    }//GEN-LAST:event_buttonRecordActionPerformed

    private void buttonTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrainActionPerformed
        if (w.getLearningManager().getLearningState() == LearningManager.LearningState.TRAINING) {
            w.getLearningManager().cancelTraining();
        } else {
            w.getLearningManager().buildAll();
        }
    }//GEN-LAST:event_buttonTrainActionPerformed

    private void buttonDeleteLastRecordingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLastRecordingActionPerformed
        w.getDataManager().deleteTrainingRound(lastRoundAdvertised);
        int numDeleted = w.getDataManager().getNumDeletedTrainingRound();
         w.getStatusUpdateCenter().update(this, "Recording set #" + lastRoundAdvertised + " (" + numDeleted + " examples) deleted.");
        if (numDeleted > 0) {
            updateReAddButton(lastRoundAdvertised);
        }
        updateDeleteLastRoundButton();
    }//GEN-LAST:event_buttonDeleteLastRecordingActionPerformed

    private void updateReAddButton(int whichRound) {
        buttonReAddLastRecording.setText("Re-add last recording (#" + whichRound + ")");
        buttonReAddLastRecording.setEnabled(true);
    }

    private void buttonReAddLastRecordingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonReAddLastRecordingActionPerformed
        int num = w.getDataManager().getNumDeletedTrainingRound();
        w.getDataManager().reAddDeletedTrainingRound();
        w.getStatusUpdateCenter().update(this, "Last recording set restored: undeleted " + num + " examples");
        updateDeleteLastRoundButton();
        buttonReAddLastRecording.setText("Re-add last recording");
        buttonReAddLastRecording.setEnabled(false);
    }//GEN-LAST:event_buttonReAddLastRecordingActionPerformed

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        if (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING) {
            w.getLearningManager().stopRecording();
        }

        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.NOT_RUNNING) {
            w.getLearningManager().setRunningState(LearningManager.RunningState.RUNNING);
        } else {
            w.getLearningManager().setRunningState(LearningManager.RunningState.NOT_RUNNING);
        }
    }//GEN-LAST:event_buttonRunActionPerformed

    private void indicatorOscInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscInMouseClicked
        w.getMainGUI().showOSCReceiverWindow();
    }//GEN-LAST:event_indicatorOscInMouseClicked

    private void indicatorOscOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscOutMouseClicked
        w.getMainGUI().showOutputTable();
    }//GEN-LAST:event_indicatorOscOutMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteLastRecording;
    private javax.swing.JButton buttonReAddLastRecording;
    private javax.swing.JButton buttonRecord;
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonTrain;
    private javax.swing.JLabel indicatorOscIn;
    private javax.swing.JLabel indicatorOscOut;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JPanel panelStatus;
    private wekimini.gui.SimpleLearningSet simpleLearningSet1;
    // End of variables declaration//GEN-END:variables

    public void setPerfomanceMode(boolean selected) {
        if (selected) {
            simpleLearningSet1.setVisible(false);
            panelStatus.setVisible(false);
            this.setSize(jPanel2.getPreferredSize());
        } else {
            simpleLearningSet1.setVisible(true);
            panelStatus.setVisible(true);
            this.setSize(getPreferredSize());
        }
    }
}
