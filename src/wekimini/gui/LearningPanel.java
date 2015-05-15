package wekimini.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.LearningManager;
import wekimini.Path;
import wekimini.TrainingRunner;
import wekimini.Wekinator;

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

    /**
     * Creates new form TestLearningPanel1
     */
    public LearningPanel() {
        initComponents();
    }

    public void setup(Wekinator w, Path[] ps, String[] modelNames) {
        this.w = w;

        simpleLearningSet1.setup(w, ps, modelNames);
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
        
        setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
        setButtonsForLearningState();
    }

    private void learningCancelled() {
        setStatus("Training was cancelled.");
    }
    
    private void trainerUpdated(TrainingRunner.TrainingStatus newStatus) {
        String s = newStatus.getNumTrained() + " of " + newStatus.getNumToTrain() 
                + " models trained, " + newStatus.getNumErrorsEncountered() + 
                " errors encountered.";
        if (newStatus.isWasCancelled()) {
            s += " Training cancelled.";
        }
        setStatus(s);
    }
    
    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == LearningManager.PROP_RECORDINGSTATE) {
            updateRecordingButton();
            updateButtonStates();
        } else if (evt.getPropertyName() == LearningManager.PROP_LEARNINGSTATE) {
            setButtonsForLearningState();
            updateStatusForLearningState();
            System.out.println("Learning state updated: " + w.getLearningManager().getLearningState());
        } else if (evt.getPropertyName() == LearningManager.PROP_RUNNINGSTATE) {
            updateRunButtonAndText();
        } else if (evt.getPropertyName() == LearningManager.PROP_NUMEXAMPLESTHISROUND) {
            setStatus(w.getLearningManager().getNumExamplesThisRound() + " new examples recorded");
        }
//Could do training state for GUI too... Want to have singleton Training Worker so all GUI elements can access update info
    }

    private void updateRunButtonAndText() {
        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING) {
            buttonRun.setText("Stop running");
            setStatus("Running.");
        } else {
            buttonRun.setText("Run");
            setStatus("Stopped running.");
        }
    }
    
    private void updateStatusForTrainingWorker() {
        
    }
    
    private void updateStatusForLearningState() {
        LearningManager.LearningState ls = w.getLearningManager().getLearningState();
        if (ls == LearningManager.LearningState.NOT_READY_TO_TRAIN) {
            setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
        } else if (ls == LearningManager.LearningState.TRAINING) {
            setStatus("Training...");
        } else if (ls == LearningManager.LearningState.DONE_TRAINING) {
            if (w.getTrainingRunner().wasCancelled()) {
                setStatus("Training was cancelled.");
            } else {
                int n = w.getLearningManager().numRunnableModels();
                if (n > 0) {
                    setStatus("Training completed. Press \"Run\" to run trained models.");
                } else {
                    setStatus("No models are ready to run. Record data and/or train.");
                }
           }
        } else if (ls == LearningManager.LearningState.READY_TO_TRAIN) {
            setStatus("Examples recorded. Press \"Train\" to build models from data.");
        }
    }

    private void setButtonsForLearningState() {
        LearningManager.LearningState ls = w.getLearningManager().getLearningState();
        if (ls == LearningManager.LearningState.NOT_READY_TO_TRAIN) {
            buttonTrain.setText("Train");
            buttonTrain.setEnabled(false);
        } else if (ls == LearningManager.LearningState.TRAINING) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Cancel training");
            buttonRecord.setEnabled(false);
            buttonRun.setEnabled(false);
        } else if (ls == LearningManager.LearningState.DONE_TRAINING) {
            buttonTrain.setEnabled(true); //Don't prevent immediate retraining; some model builders may give different models on same data.
            buttonTrain.setText("Train");
            buttonRecord.setEnabled(true);
            buttonRun.setEnabled(true);
        } else if (ls == LearningManager.LearningState.READY_TO_TRAIN) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Train");
            buttonRecord.setEnabled(true);
            buttonRun.setEnabled(false);
        }
    }

    private void updateRecordingButton() {
        if (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING) {
            buttonRecord.setText("Stop Recording");
        } else {
            buttonRecord.setText("Start Recording");
        }
    }

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
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        simpleLearningSet1 = new wekimini.gui.SimpleLearningSet();
        jPanel3 = new javax.swing.JPanel();
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

        jButton4.setText("Undo last recording round");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Redo last recording round");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(buttonRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonTrain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonTrain, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(0, 0, 0)
                .addComponent(jButton5))
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
                .addComponent(simpleLearningSet1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(85, Short.MAX_VALUE))
            .addComponent(simpleLearningSet1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Status:");

        labelStatus.setText(" ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator3)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonStates() {
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
            setStatus("Recording - waiting for inputs to arrive");
        } else {
            w.getLearningManager().stopRecording();
            setStatus("Examples recorded. Press \"Train\" to build models from data.");
        }
    }//GEN-LAST:event_buttonRecordActionPerformed

    private void buttonTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrainActionPerformed
        if (w.getLearningManager().getLearningState() == LearningManager.LearningState.TRAINING) {
            w.getLearningManager().cancelTraining();
        } else {
            w.getLearningManager().buildAll();
        }
    }//GEN-LAST:event_buttonTrainActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonRecord;
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonTrain;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelStatus;
    private wekimini.gui.SimpleLearningSet simpleLearningSet1;
    // End of variables declaration//GEN-END:variables
}